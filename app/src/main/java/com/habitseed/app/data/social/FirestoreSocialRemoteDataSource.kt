package com.habitseed.app.data.social

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.habitseed.app.data.social.dto.FollowingDto
import com.habitseed.app.data.social.dto.NudgeDto
import com.habitseed.app.data.social.dto.PublicProfileDto
import com.habitseed.app.data.social.dto.toFirestoreCreateData
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreSocialRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : SocialRemoteDataSource {
    override suspend fun upsertPublicProfile(profile: PublicProfileDto) {
        firestore.collection(PUBLIC_PROFILES)
            .document(profile.uid)
            .set(profile)
            .await()
    }

    override suspend fun getPublicProfile(uid: String): PublicProfileDto? {
        return firestore.collection(PUBLIC_PROFILES)
            .document(uid)
            .get()
            .await()
            .toObject(PublicProfileDto::class.java)
    }

    override suspend fun getLeaderboard(limit: Long): List<PublicProfileDto> {
        return firestore.collection(PUBLIC_PROFILES)
            .orderBy("weeklyCompletionRate", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(PublicProfileDto::class.java) }
    }

    override suspend fun getFollowing(currentUid: String): List<FollowingDto> {
        return firestore.collection(USERS)
            .document(currentUid)
            .collection(FOLLOWING)
            .orderBy("followedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(FollowingDto::class.java) }
    }

    override suspend fun followUser(currentUid: String, following: FollowingDto) {
        firestore.collection(USERS)
            .document(currentUid)
            .collection(FOLLOWING)
            .document(following.targetUid)
            .set(following)
            .await()
    }

    override suspend fun unfollowUser(currentUid: String, targetUid: String) {
        firestore.collection(USERS)
            .document(currentUid)
            .collection(FOLLOWING)
            .document(targetUid)
            .delete()
            .await()
    }

    override suspend fun sendNudge(nudge: NudgeDto) {
        firestore.collection(NUDGES)
            .add(nudge.toFirestoreCreateData())
            .await()
    }

    override suspend fun getUnreadNudges(toUid: String, limit: Long): List<NudgeDto> {
        return firestore.collection(NUDGES)
            .whereEqualTo("toUid", toUid)
            .limit(limit)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(NudgeDto::class.java)
                    ?.copy(id = document.id)
            }
            .filter { it.readAt == null }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun markNudgeRead(nudgeId: String, readAt: Long) {
        if (nudgeId.isBlank()) return
        firestore.collection(NUDGES)
            .document(nudgeId)
            .update("readAt", readAt)
            .await()
    }

    private companion object {
        const val PUBLIC_PROFILES = "public_profiles"
        const val USERS = "users"
        const val FOLLOWING = "following"
        const val NUDGES = "nudges"
    }
}
