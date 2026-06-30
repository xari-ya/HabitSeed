package com.habitseed.app.data.social.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NudgeDtoTest {
    @Test
    fun toFirestoreCreateData_omitsLocalDocumentId() {
        val nudge = NudgeDto(
            id = "local-document-id",
            fromUid = "sender",
            toUid = "recipient",
            fromName = "Ari",
            fromPhotoUrl = null,
            messageType = NudgeMessageTypes.GARDEN_NUDGE,
            message = "Keep your garden growing.",
            createdAt = 123L,
            readAt = null
        )

        val data = nudge.toFirestoreCreateData()

        assertFalse(data.containsKey("id"))
        assertEquals(
            setOf(
                "fromUid",
                "toUid",
                "fromName",
                "fromPhotoUrl",
                "messageType",
                "message",
                "createdAt",
                "readAt"
            ),
            data.keys
        )
        assertEquals("sender", data["fromUid"])
        assertEquals("recipient", data["toUid"])
        assertEquals("Ari", data["fromName"])
        assertTrue(data.containsKey("fromPhotoUrl"))
        assertNull(data["fromPhotoUrl"])
        assertEquals(NudgeMessageTypes.GARDEN_NUDGE, data["messageType"])
        assertEquals("Keep your garden growing.", data["message"])
        assertEquals(123L, data["createdAt"])
        assertTrue(data.containsKey("readAt"))
        assertNull(data["readAt"])
    }
}
