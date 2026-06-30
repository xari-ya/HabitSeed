# HabitSeed System Report

This report is based on the live code in the current checkout.

## 1. What the system is

HabitSeed is a local-first Android habit tracking application built with Kotlin, Jetpack Compose, Room, Hilt, Navigation Compose, Firebase Auth, and Firebase Firestore.

At a product level, it lets a user:

- create habits
- mark habits complete once per day
- earn water drops for completion
- grow plant visuals tied to habit progress
- see recent statistics and streaks
- buy plant unlocks from a store
- manage profile/settings
- optionally sign in with Google and publish a limited public garden summary to Firebase for leaderboard/following/nudge features

The core private habit data remains in the local Room database. Firebase is only used for identity and public social data.

## 2. Architecture and runtime flow

The app follows a layered MVVM-style structure:

- `ui/`: Compose screens, components, navigation, and view models
- `domain/`: repository contracts, date/progress logic, social summary mapping and sync policy
- `data/local/`: Room entities, DAOs, database, and query models
- `data/repository/`: repository implementations over Room
- `data/auth/`: Google/Firebase sign-in bridge
- `data/social/`: Firestore public-profile/following/nudge sync
- `di/`: Hilt dependency wiring

Main runtime flow:

1. `MainActivity` starts Compose and reads `UserSettingsEntity` to decide dark/light theme.
2. `SplashViewModel` decides where to navigate:
   - signed-in Google user -> `Home`
   - onboarding done but not signed in -> `Login`
   - first-time user -> `Onboarding`
3. Main screens are hosted under `MainScreen` + `HabitSeedNavGraph`.
4. Most screen state comes from Room `Flow`s exposed through repositories and `StateFlow`s in view models.
5. Social screens add Firebase/Firestore on top of that local base.

## 3. Data used by the system and where it comes from

### 3.1 Local Room database

Database: `habitseed_db`

Defined in `data/local/AppDatabase.kt`.

Primary local tables:

- `users`: `UserEntity`
- `user_settings`: `UserSettingsEntity`
- `habits`: `HabitEntity`
- `habit_logs`: `HabitLogEntity`
- `plant_types`: `PlantTypeEntity`
- `shop_items`: `ShopItemEntity`
- `purchases`: `PurchaseEntity`
- `user_unlocked_plants`: `UserUnlockedPlantEntity`
- `friends`: `FriendEntity`
- `friend_nudges`: `FriendNudgeEntity`
- `cached_leaderboard_profiles`: `CachedLeaderboardProfileEntity`
- `cached_following_profiles`: `CachedFollowingProfileEntity`
- `social_cache_metadata`: `SocialCacheMetadataEntity`

Seed data inserted on first database creation in `DatabaseModule.provideAppDatabase()`:

- local demo user: `Alex`, `alex@example.com`, `1250` water drops
- default settings
- default unlocked plant: `succulent`
- plant catalog
- shop catalog
- three mock friends: `Sam`, `Maya`, `Noah`

### 3.2 Firebase Auth

Used by:

- `FirebaseAuthRepository`
- `GoogleAuthClient`

Data obtained:

- Firebase UID
- Google display name
- Google email
- Google photo URL
- email verification status

This data is merged into local `UserEntity` by `UserRepositoryImpl.upsertGoogleUser()`.

### 3.3 Firebase Firestore

Used by `FirestoreSocialRemoteDataSource`.

Collections used:

- `public_profiles`
- `users/{uid}/following`
- `nudges`

Public social data stored there:

- public garden summary (`PublicProfileDto`)
- followed-user snapshots (`FollowingDto`)
- nudge events (`NudgeDto`)

### 3.4 Static app resources

Used from `app/src/main/res/`.

Important data assets:

- plant images in `drawable-nodpi/`
  - `plant_succulent.png`
  - `plant_starter_fern.png`
  - `plant_desert_cactus.png`
  - `plant_monstera.png`
  - `plant_water_lily.png`
  - `plant_golden_bonsai.png`
- branding images
  - `seed_logo_transparent.png`
  - `seed_splash.png`
- application label in `values/strings.xml`

### 3.5 Build and library configuration

`app/build.gradle.kts` declares:

- Compose UI
- Material 3
- Room
- Hilt
- Navigation Compose
- Firebase Auth + Firestore
- Google Credential Manager
- Coil
- JUnit and Android test libraries

## 4. Entry point, app shell, and dependency wiring

### `HabitSeedApplication`

- Class: `HabitSeedApplication : Application`
- Role: enables Hilt app-wide injection through `@HiltAndroidApp`
- Methods: no custom methods

### `MainActivity`

- Class: `MainActivity : ComponentActivity`
- Method: `onCreate(savedInstanceState)`
  - starts Compose
  - reads `settings` from `MainActivityViewModel`
  - applies `HabitSeedTheme`
  - loads `MainScreen`

### `MainActivityViewModel`

- Class: `MainActivityViewModel`
- State:
  - `settings: StateFlow<UserSettingsEntity?>`
- Role:
  - exposes the current settings row from Room for top-level theme selection

### `FirebaseModule`

- Object: Hilt provider module
- Methods:
  - `provideFirebaseAuth()`: returns `FirebaseAuth.getInstance()`
  - `provideFirebaseFirestore()`: returns `FirebaseFirestore.getInstance()`
  - `provideCredentialManager(context)`: returns Android `CredentialManager`

### `RepositoryModule`

- Abstract Hilt binding module
- Methods:
  - `bindUserRepository()`
  - `bindHabitRepository()`
  - `bindShopRepository()`
  - `bindSocialRepository()`
  - `bindAuthRepository()`
  - `bindSocialRemoteDataSource()`
- Role:
  - binds interfaces to their concrete implementations

### `DatabaseModule`

- Object: Hilt Room provider module
- Methods:
  - `provideAppDatabase(context)`: builds Room database, migrations, seed callback
  - `seedStoreCatalogIfNeeded(db)`: ensures current store catalog exists
  - `isStoreCatalogCurrent(db)`: checks whether current catalog data is already present
  - `seedStoreCatalog(db)`: inserts/activates supported plant/store items
  - DAO provider methods: `provideUserDao`, `provideHabitDao`, and other DAO getters
- Data used:
  - Room database file
  - hard-coded SQL seed values

## 5. Authentication layer

### `AuthRepository`

- Interface methods:
  - `currentUser()`
  - `isSignedInWithGoogle()`
  - `signInWithGoogle(context)`
  - `signOut()`

### `AuthUser`

- Data class representing authenticated identity
- Fields:
  - `uid`
  - `displayName`
  - `email`
  - `photoUrl`
  - `isEmailVerified`

### `FirebaseAuthRepository`

- Class implementing `AuthRepository`
- Methods:
  - `currentUser()`: converts the current Firebase user to `AuthUser`
  - `isSignedInWithGoogle()`: checks if `FirebaseAuth.currentUser` exists
  - `signInWithGoogle(context)`: delegates to `GoogleAuthClient`
  - `signOut()`: signs out Firebase and clears Google credential state
  - private `FirebaseUser.toAuthUser()`: maps Firebase user fields into `AuthUser`

### `GoogleAuthClient`

- Class that performs credential-based Google sign-in
- Methods:
  - `signIn(context)`
    - configures `GetGoogleIdOption`
    - opens Android Credential Manager
    - validates returned credential type
    - extracts Google ID token
    - signs into Firebase with `GoogleAuthProvider`
    - returns `AuthUser`
    - maps cancellation, parsing, missing account, and generic errors to `AuthException`
  - `clearCredentialState()`: clears cached credential state from `CredentialManager`
  - private `FirebaseUser.toAuthUser()`: same mapping helper as above
- Exception:
  - `AuthException(message)`

## 6. Local data model and DAO behavior

### Core entities

- `UserEntity`
  - stores profile, auth linkage, drops, streaks, theme, onboarding, sync metadata
  - computed aliases: `username`, `seeds`, `streak`
- `UserSettingsEntity`
  - stores app toggles: notifications, reminder time, dark mode, sound, haptics
- `HabitEntity`
  - stores one habit with icon, color, frequency, streaks, total completions, plant type, growth level
  - computed aliases: `title`, `frequency`, `plantType`
- `HabitLogEntity`
  - stores one completion record per habit/date
  - unique `(habitId, dateKey)` index prevents duplicates
  - computed aliases: `timestamp`, `isCompleted`, `notes`
- `PlantTypeEntity`
  - stores plant catalog metadata
- `ShopItemEntity`
  - store item definition
  - computed aliases: `price`, `type`
- `PurchaseEntity`
  - tracks which user purchased which store item
- `UserUnlockedPlantEntity`
  - tracks unlocked plants by user
- `FriendEntity`
  - mock local social preview friend
- `FriendNudgeEntity`
  - local nudge log for mock friends
- `CachedLeaderboardProfileEntity`
  - local cache of remote public profiles
  - methods:
    - `toDto()`
    - `fromDto(profile, rank, cachedAt)`
- `CachedFollowingProfileEntity`
  - local cache of followed users
  - methods:
    - `toDto()`
    - `fromDto(following, cachedAt)`
- `SocialCacheMetadataEntity`
  - stores refresh timestamp per cache key

### Query models

- `TodayHabitStatus`
  - result model combining `HabitEntity` with `isCompletedToday` and `completedAt`
- `ShopItemWithStatus`
  - result model combining store item with purchase state and rarity
- `DailyCompletionCount`
  - date/count aggregate result from habit logs

### DAO behavior

- `UserDao`
  - `getUser()`, `observeCurrentUser()`: stream first user row
  - `getUserById(userId)`: fetch single user
  - `insertUser(user)`, `updateUser(user)`
  - `addWaterDrops(userId, amount, updatedAt)`: increments/decrements only if result stays non-negative
  - `markOnboardingComplete(...)`
  - `updateStreaks(...)`
  - `updateLastCloudSyncAt(...)`

- `UserSettingsDao`
  - `getSettings(userId)`
  - `insertSettings(settings)`
  - `updateSettings(settings)`

- `HabitDao`
  - `getAllHabits(userId)`: active habits ordered by newest
  - `getHabitById(id)`
  - `insertHabit(habit)`
  - `updateHabit(habit)`
  - `archiveHabit(habitId, updatedAt)`
  - `observeTodayHabitsWithCompletionStatus(userId, dateKey)`: left-joins today’s log state onto habit rows

- `HabitLogDao`
  - `getRecentLogsForHabit(habitId, limit)`
  - `insertLog(log)`
  - `getLogForHabitAndDate(habitId, dateKey)`
  - `isHabitCompletedOnDate(habitId, dateKey)`
  - `getLatestCompletedDateKeyBefore(habitId, beforeDateKey)`
  - `getDailyCompletionCounts(fromDateKey, toDateKey)`

- `PlantTypeDao`
  - `getPlantTypes()`
  - `insertPlantTypes(items)`

- `ShopItemDao`
  - `getAllShopItems(userId)`: joins items, purchases, and plant rarity
  - `getShopItemById(shopItemId)`
  - `insertShopItems(items)`
  - `updateShopItem(item)`

- `PurchaseDao`
  - `getPurchase(userId, shopItemId)`
  - `insertPurchase(purchase)`
  - `insertUnlockedPlant(unlockedPlant)`

- `FriendDao`
  - `getFriends()`
  - `insertFriends(friends)`

- `FriendNudgeDao`
  - `insertNudge(nudge)`

- `CachedLeaderboardProfileDao`
  - `observeProfiles()`
  - `clear()`
  - `insertProfiles(profiles)`
  - `replaceAll(profiles)`: clears then inserts

- `CachedFollowingProfileDao`
  - `observeProfiles()`
  - `clear()`
  - `deleteByTargetUid(targetUid)`
  - `insertProfile(profile)`
  - `insertProfiles(profiles)`
  - `replaceAll(profiles)`

- `SocialCacheMetadataDao`
  - `getMetadata(cacheKey)`
  - `upsert(metadata)`

## 7. Repository layer behavior

### `UserRepository`

- Interface methods:
  - `getUser()`
  - `getSettings()`
  - `insertUser(user)`
  - `updateUser(user)`
  - `addWaterDrops(amount)`
  - `markOnboardingComplete()`
  - `updateSettings(settings)`
  - `upsertGoogleUser(authUser)`
  - `updateLastCloudSyncAt(timestamp, publicProfileSyncHash)`

### `UserRepositoryImpl`

- Methods:
  - `getUser()`, `getSettings()`, `insertUser()`, `updateUser()`: direct DAO passthroughs
  - `addWaterDrops(amount)`: updates current local user drops
  - `markOnboardingComplete()`: marks user as onboarded
  - `updateSettings(settings)`: inserts/replaces settings row
  - `upsertGoogleUser(authUser)`
    - merges Firebase/Google identity into local `UserEntity`
    - preserves a real existing custom name
    - uses Google display name when available
    - derives a readable name from email local-part if display name is missing
    - falls back to `Gardener`
    - preserves existing avatar unless local avatar is blank
    - sets provider to `google`, updates verification, login time, and onboarding flag
  - `updateLastCloudSyncAt(timestamp, publicProfileSyncHash)`: stores public sync metadata
  - private `isUserDefinedName(name)`: rejects placeholders like `Alex` or `Gardener`
  - private `deriveNameFromEmail(email)`: converts `john.doe@example.com` -> `John Doe`

### `HabitRepository`

- Interface methods:
  - `getAllHabits()`
  - `getTodayHabitsWithCompletionStatus(dateKey)`
  - `getHabitById(id)`
  - `insertHabit(habit)`
  - `updateHabit(habit)`
  - `archiveHabit(habitId)`
  - `getRecentLogsForHabit(habitId, limit)`
  - `completeHabit(habitId, dateKey)`
  - `isHabitCompletedOnDate(habitId, dateKey)`
  - `getStatsForLast30Days()`

### `HabitRepositoryImpl`

- Methods:
  - standard read/write passthroughs for habits and logs
  - `completeHabit(habitId, dateKey)`
    - transactionally:
      - loads habit
      - blocks duplicate completion on same date
      - inserts `HabitLogEntity` with `+10` water drops
      - computes new habit streak from previous completed date
      - increments habit totals and growth level
      - adds `10` drops to user
      - updates user streak summary
    - returns `true` only if the completion succeeded
  - `getStatsForLast30Days()`
    - reads daily aggregates from `HabitLogDao`
    - fills missing days with zero counts
    - returns a fixed 30-day sequence of `DailyCompletionStat`

### `ShopRepository`

- Interface methods:
  - `getAllShopItems()`
  - `insertShopItems(items)`
  - `updateShopItem(item)`
  - `purchaseShopItem(userId, shopItemId)`

### `ShopRepositoryImpl`

- Methods:
  - read/write passthroughs for store items
  - `purchaseShopItem(userId, shopItemId)`
    - transactionally:
      - loads user and item
      - rejects inactive items
      - rejects duplicates
      - rejects insufficient drops
      - deducts price from user
      - inserts purchase
      - unlocks linked plant if present
    - returns `true` on success
- Helper exception:
  - `PurchaseRollbackException` used to force transaction failure when purchase insert fails

### `SocialRepository`

- Interface methods:
  - `getFriends()`
  - `sendNudge(friendId, message)`

### `SocialRepositoryImpl`

- Methods:
  - `getFriends()`: streams local mock friends
  - `sendNudge(friendId, message)`: inserts a local `FriendNudgeEntity`

## 8. Social remote layer and public-profile logic

### `SocialRemoteDataSource`

- Interface methods:
  - `upsertPublicProfile(profile)`
  - `getPublicProfile(uid)`
  - `getLeaderboard(limit)`
  - `getFollowing(currentUid)`
  - `followUser(currentUid, following)`
  - `unfollowUser(currentUid, targetUid)`
  - `sendNudge(nudge)`

### `FirestoreSocialRemoteDataSource`

- Implements Firestore access for all public social features
- Methods:
  - `upsertPublicProfile(profile)`: writes `public_profiles/{uid}`
  - `getPublicProfile(uid)`: fetches one public profile
  - `getLeaderboard(limit)`: orders `public_profiles` by `weeklyCompletionRate desc`
  - `getFollowing(currentUid)`: reads `users/{uid}/following`
  - `followUser(currentUid, following)`: writes followed user snapshot
  - `unfollowUser(currentUid, targetUid)`: deletes followed user snapshot
  - `sendNudge(nudge)`: inserts into top-level `nudges`

### DTOs

- `PublicProfileDto`
  - public social summary fields only
- `FollowingDto`
  - followed-user snapshot
- `NudgeDto`
  - one nudge event
- `NudgeMessageTypes`
  - constants:
    - `WATER_REMINDER`
    - `KEEP_GOING`
    - `STREAK_CHEER`
    - `GARDEN_NUDGE`
  - `allowedTypes`: validation set

### `SocialSyncRepository`

- This is the central bridge between local habit data and Firestore social data.
- Public methods:
  - `observeCachedLeaderboard()`: streams cached leaderboard DTOs from Room
  - `observeCachedFollowing()`: streams cached following DTOs from Room
  - `syncPublicProfile(reason, force)`
    - requires signed-in Firebase user
    - reads local `UserEntity`, all habits, and 30-day stats
    - converts them to `PublicProfileDto`
    - computes public profile hash
    - asks `PublicProfileSyncPolicy` whether to write
    - if allowed, upserts Firestore public profile and updates local sync metadata
  - `loadLeaderboard()`
    - requires Google sign-in
    - pulls remote leaderboard
    - caches results locally with rank and cache time
  - `loadFollowing()`
    - requires current Firebase UID
    - pulls remote following list
    - caches locally
  - `addFriendByUid(targetUid)`
    - validates non-empty UID and prevents following self
    - fetches target public profile
    - creates `FollowingDto` snapshot
    - writes it to Firestore and local cache
  - `unfollow(targetUid)`
    - removes following entry remotely and from local cache
  - `sendNudge(targetUid, messageType, message)`
    - validates auth, target UID, and allowed message type
    - builds `NudgeDto` using local user name/avatar fallback chain
    - writes remote nudge
- Supporting types:
  - `PublicProfileSyncReason`
    - `SIGN_IN`, `APP_START`, `HABIT_COMPLETED`, `PROFILE_EDIT`
  - `PublicProfileSyncResult`
    - `didWrite`, `reason`, `message`
  - `SocialException(message)`

### `SocialSummaryMapper`

- Method: `toPublicProfile(firebaseUid, user, habits, stats, existingCreatedAt, now)`
- Role:
  - converts private local state into a minimal public social profile
- Data included in public profile:
  - public name
  - avatar URL
  - current/best streak
  - fully grown plant count
  - weekly completion rate
  - total completions
  - last active date
  - created/updated timestamps
- Data deliberately not exposed:
  - private habit titles
  - habit descriptions
  - email
  - water drops

### `PublicProfileSyncPolicy`

- Method: `shouldSync(reason, previousHash, lastSyncedAt, nextHash, now)`
  - first sync always writes
  - unchanged hash skips
  - `SIGN_IN` and `PROFILE_EDIT`: write immediately when hash changed
  - `APP_START`: write only if at least 24h since last sync
  - `HABIT_COMPLETED`: write at most once per local day when hash changed
- Method: `syncHash(profile)`
  - SHA-256 hash of public-facing profile fields only

## 9. Domain utilities

### `DateUtils`

- `getStartOfDay(timestamp)`: local midnight millis
- `getEndOfDay(timestamp)`: local day-end millis
- `getDateKey(timestamp)`: `YYYY-MM-DD`
- `todayDateKey()`
- `parseDateKey(dateKey)`
- `isPreviousDateKey(previousDateKey, currentDateKey)`: checks consecutive-day relation

### `HabitProgressCalculator`

- `calculateNextStreak(lastCompletedDateKey, currentDateKey, currentStreak)`
  - first completion -> `1`
  - same-day repeat -> no change
  - consecutive day -> increment
  - missed gap -> reset to `1`
- `calculateGrowthLevel(totalCompletions)`
  - one growth level per 5 completions
  - capped at `4`

### `StreakCalculator`

- `calculateNewStreak(currentStreak, isCompleted, missedDays)`
- Present but not used in the current main flow
- Simpler demonstration logic than `HabitProgressCalculator`

### `DailyCompletionStat`

- simple domain model for a date key and completion count

## 10. Navigation, screens, and UI behavior

### Navigation shell

- `Screen`
  - route constants for:
    - `Splash`
    - `Onboarding`
    - `Login`
    - `Home`
    - `HabitDetail`
    - `Stats`
    - `Store`
    - `Social`
    - `Profile`
    - `EditProfile`
    - `AddHabit`
  - method:
    - `HabitDetail.createRoute(habitId)`

- `MainScreen()`
  - creates nav controller
  - defines bottom navigation items
  - shows bottom bar only on main tab routes
  - hosts `HabitSeedNavGraph`

- `HabitSeedNavGraph(navController, startDestination, modifier)`
  - defines all route destinations and screen-to-screen navigation behavior

### Splash

- `SplashViewModel`
  - state: `destination: StateFlow<SplashDestination?>`
  - init flow:
    - reads local user from Room
    - reads current Firebase auth user
    - if signed in, merges auth user into local profile if needed
    - triggers background public-profile sync on app start
    - delays for splash branding
    - chooses destination
- `SplashDestination`
  - `Onboarding`, `Login`, `Home`
- `SplashScreen(viewModel, ...)`
  - watches `destination`
  - navigates when destination becomes non-null
  - temporarily changes system bar colors
  - shows app logo and title

### Onboarding

- `OnboardingScreen(onGetStarted, modifier)`
  - introduction screen
  - button moves user to login
- private helpers:
  - `OnboardingDots()`
  - `DecorativeLeaves()`

### Login

- `LoginViewModel`
  - state:
    - `uiState: StateFlow<LoginUiState>`
    - `events: SharedFlow<LoginEvent>`
  - method:
    - `onGoogleLoginClicked(context)`
      - blocks duplicate taps while loading
      - signs in with Google
      - persists/merges auth user into local `UserEntity`
      - triggers public-profile sync for sign-in
      - emits navigation or error events
- `AuthState`
  - `Idle`, `Loading`, `Success`, `Error`
- `LoginUiState`
  - `authState`, `errorMessage`, computed `isLoading`
- `LoginEvent`
  - `NavigateHome`
  - `ShowMessage(message)`
- `LoginScreen(onLoginSuccess, viewModel)`
  - renders Google sign-in UI
  - collects events for snackbar and navigation

### Home

- `HomeViewModel`
  - combines:
    - current user
    - today’s habits with completion status
  - emits `HomeUiState`
- `HomeUiState`
  - `user`
  - `todayHabits`
  - `completedToday`
  - `scheduledToday`
  - `progressPercent`
- `HomeScreen(onNavigateToHabitDetail, viewModel)`
  - main dashboard
  - shows greeting, hero card, habit list
  - opens `AddHabitSheet`
- private UI helpers:
  - `GreetingHeader(name, waterDrops)`
  - `DashboardHeroCard(...)`
  - `EmptyGardenCard()`
  - `TodayHabitRow(habitStatus, onClick)`
  - `StatusPill(text, background, contentColor)`
  - `iconForHabit(iconName)`
  - `colorFromHex(colorHex, fallback)`
  - `greetingForTime()`
  - `firstNameOrFallback(name)`
  - `encouragementText(progressPercent, scheduledToday)`

### Add Habit

- `AddHabitViewModel`
  - form state flows:
    - `title`
    - `description`
    - `frequency`
    - `selectedPlant`
    - `selectedIcon`
    - `selectedColor`
    - `reminderEnabled`
  - methods:
    - `updateTitle`
    - `updateDescription`
    - `updateFrequency`
    - `updateSelectedPlant`
    - `updateSelectedIcon`
    - `updateSelectedColor`
    - `updateReminderEnabled`
    - `resetForm()`
    - `saveHabit(onSuccess)`
      - validates non-blank title
      - builds `HabitEntity`
      - maps user-visible plant label to stored plant type ID
      - writes to Room
      - resets form and runs callback
    - private `plantTypeIdFor(label)`

- `AddHabitScreen(onNavigateBack, viewModel)`
  - full-screen habit creation route
- `AddHabitSheet(onDismiss, viewModel)`
  - bottom-sheet version used from Home
- private helpers:
  - `AddHabitForm(...)`
  - `SectionLabel(text)`
  - `IconChoiceCard(option, selected, onClick)`
  - `ColorChoice(option, selected, onClick)`
- private option models/constants:
  - `HabitIconOption`
  - `HabitColorOption`
  - `habitIconOptions`
  - `habitColorOptions`

### Habit Detail

- `HabitDetailViewModel`
  - reads `habitId` from `SavedStateHandle`
  - state:
    - `habit`
    - `isCompletedToday`
    - `events`
    - combined `uiState`
  - methods:
    - init -> `loadHabit()` and `checkCompletionStatus()`
    - `loadHabit()`
    - `checkCompletionStatus()`
    - `completeHabit()`
      - prevents duplicate local completion
      - completes habit through repository
      - refreshes local habit state
      - marks today complete
      - syncs public social profile for `HABIT_COMPLETED`
      - emits success message
- `HabitDetailUiState`
  - `habit`
  - `isCompletedToday`
  - `recentLogs`
  - `completionRate`
- `HabitDetailScreen(onNavigateBack, viewModel)`
  - renders plant hero section, stats, recent watering strip, and swipe-to-complete control
- private helpers:
  - `DailyLogStrip(logs)`
  - `CelebrationBubble()`
  - `growthLabel(level)`

### Stats

- `StatsViewModel`
  - combines user, habits, and 30-day daily stats
  - computes:
    - `currentWeek`
    - `plantsFullyGrown`
    - `averageDailyCompletions`
  - private helper:
    - `currentWeekDates()`
- `StatsUiState`
  - `user`
  - `habits`
  - `dailyStats`
  - `currentWeek`
  - `currentStreak`
  - `plantsFullyGrown`
  - `averageDailyCompletions`
- `StatsScreen(viewModel)`
  - renders week strip, monthly chart, and summary cards
- private helpers:
  - `WeekStrip(dates, highlight)`
  - `StatsChartCard(stats, averageDailyCompletions)`
  - `CompletionLineChart(stats)`
  - `HarvestStatCard(label, value, modifier)`

### Store

- `ShopViewModel`
  - state:
    - `user`
    - `shopItems`
    - `messages`
  - method:
    - `purchaseItem(item)`
      - checks ownership and affordability
      - calls repository purchase flow
      - emits result messages
- `ShopScreen(viewModel)`
  - renders water drops header, rarity filters, and store grid
- private helpers:
  - `StoreHeader(waterDrops)`
  - `StorePlantTile(item, canAfford, onPurchase)`
  - `rarityUi(rarity)`
- private UI types:
  - `StoreFilter`
  - `RarityUi`

### Social

- `SocialViewModel`
  - state:
    - `uiState`
    - `messages`
  - init:
    - collects local mock friends
    - collects cached leaderboard
    - collects cached following list
    - records whether Google is signed in
  - methods:
    - `selectTab(tab)`
    - `refresh()`
      - rate-limited to once per minute
      - loads leaderboard or following from Firebase depending on selected tab
    - `addFriendByUid(uid)`
    - `unfollow(friend)`
    - `sendNudge(friend: FollowingDto)`: remote Firebase nudge
    - `sendNudge(friend: FriendEntity)`: local mock nudge
- `SocialTab`
  - `Leaderboard`
  - `Gardens`
- `SocialUiState`
  - selected tab, local friends, cached remote profiles, auth state, loading state, current nudge target, error message
- `SocialScreen(viewModel)`
  - if signed in:
    - leaderboard and following operate on cached/remote Firebase data
  - if not signed in:
    - shows local mock preview social data only
- private helpers:
  - `SocialHeader()`
  - `SocialTabs(selectedTab, onSelectTab)`
  - `AddFriendCard(onAddFriend)`
  - `LeaderboardActionRow(onRefresh)`
  - `AddFriendDialog(uid, onUidChange, onDismiss, onAddFriend)`
  - `LoadingSocialCard()`
  - `SocialMessageCard(title, message)`
  - `LeaderboardProfileCard(profile, rank)`
  - `FollowingCard(friend, rank, isNudgeInFlight, onNudge, onUnfollow)`
  - `PublicStat(label, value, modifier)`
  - `RemoteAvatarBadge(name, photoUrl, rank)`
  - `FriendCard(friend, rank, mode, onNudge)`
  - `StreakPill(streak)`
  - `FriendStatRow(icon, text)`
  - `friendsForTab(friends, selectedTab)`
  - `initialsFor(name)`
  - `shortUid(uid)`
  - `gardenStatus(friend)`
  - `displayPlantName(assetName)`
  - `lastActiveLabel(lastActiveDateKey)`
  - `friendshipStreak(streak)`

### Profile and settings

- `ProfileViewModel`
  - combines current user and settings into `ProfileUiState`
  - methods:
    - `toggleNotifications(enabled)`
    - `toggleDarkMode(enabled)`
    - `toggleSound(enabled)`
    - `toggleHaptics(enabled)`
    - `cycleReminderTime()`: cycles 8 AM -> 12 PM -> 6 PM -> 8 AM
    - `onMockAction(label)`
    - `showMessage(message)`
    - `saveProfile(name, avatarUrl)`
      - validates user exists and name is non-blank
      - updates local user name/avatar
      - triggers `PROFILE_EDIT` public-profile sync
      - emits local save / sync warning / saved events
    - `logout()`
      - signs out through `AuthRepository`
      - emits `Logout` event
    - private `updateSettings(transform)`
- `ProfileUiState`
  - `user`
  - `settings`
- `ProfileEvent`
  - `ShowMessage`
  - `Logout`
  - `ProfileSaved`

- `ProfileScreen(onEditProfile, onLogout, viewModel)`
  - shows avatar, email, provider, join date, stats, Firebase UID, toggles, and logout
- private helpers:
  - `ProfileHeader(...)`
  - `ProfileSummaryCard(...)`
  - `SummaryStat(...)`
  - `SettingsGroupCard(title, rows)`
  - `ActionRow(row)`
  - `FirebaseUidCard(firebaseUid, clipboardManager, onCopy)`
  - `AppSettingsCard(...)`
  - `ToggleRow(...)`
  - `resolvedRowTint(row)`
  - `resolvedRowTitleColor(row)`
  - `formatJoinDate(joinedAt)`
  - `formatReminderTime(hour, minute)`
- private row model:
  - `SettingRowAction`

- `EditProfileScreen(onNavigateBack, viewModel)`
  - editable form for display name and avatar URL
  - saves through shared `ProfileViewModel`
- private helper:
  - `EditProfilePreview(name, email, avatarUrl)`

## 11. Shared UI components and theming

### Shared components

- `GardenProgressIndicator(progress, label, modifier)`
  - generic progress bar with percentage label
- `HabitCard(habit, onClick, modifier)`
  - generic habit summary card
- `PlantAssetMapper.plantAssetFor(plantType)`
  - maps plant type IDs/aliases to drawable resource IDs
- `PlantVisualizer(plantType, growthLevel, modifier)`
  - displays a plant asset with floating animation
  - `growthLevel` is currently used only in content description, not to swap asset stages
- `SeedBadge(waterDrops, modifier)`
  - water drop badge
- `StatCard(label, value, modifier, isHighlighted)`
  - reusable stat card
- `SwipeToCompleteSlider(...)`
  - draggable slider
  - on drag end near threshold, animates to completion and calls `onComplete`

### Theme files

- `Color.kt`
  - color constants for light and dark theme palettes
- `Dimens.kt`
  - shared spacing/radius/button constants
- `Theme.kt`
  - `HabitSeedTheme(darkTheme, content)`
  - applies light/dark `MaterialTheme` and system bar colors
- `Type.kt`
  - typography tokens used by the app

## 12. Android manifest and permissions

From `AndroidManifest.xml`:

- internet permission is declared for Firebase/Auth/Coil/social features
- application class: `.HabitSeedApplication`
- launcher activity: `.MainActivity`

## 13. Tests and what they verify

### `UserRepositoryImplTest`

- `replacesSeededPlaceholderWithNameDerivedFromEmail()`
  - verifies Google sign-in can replace placeholder local name with email-derived name
- `preservesExistingCustomNameOnLaterSignIn()`
  - verifies existing custom user name is not overwritten on later sign-in

### `PublicProfileSyncPolicyTest`

- `firstSignInWritesWhenNoPriorSyncExists()`
- `appStartWithUnchangedHashSkips()`
- `habitCompletionWritesAtMostOncePerLocalDate()`
- `profileEditWritesOnlyWhenPublicHashChanges()`
- `syncHashIgnoresUpdatedAtButIncludesPublicFields()`

These validate when public social profile data should or should not be written to Firestore.

### `SocialSummaryMapperTest`

- `mapsOnlyPublicGardenSummaryFields()`

This verifies only safe, public summary data is exposed in `PublicProfileDto`.

### `DateUtilsTest`

- `dateKey_usesLocalDateFormat()`
- `startAndEndOfDay_areCorrectForLocalTimezone()`

### `HabitProgressCalculatorTest`

- `completeFirstDay_setsCurrentStreakToOne()`
- `completeConsecutiveDays_incrementsStreak()`
- `missedRequiredDay_resetsStreak()`
- `plantGrowthLevel_updatesAfterCompletions()`

## 14. Functional summary by user-facing feature

- Splash: brand screen plus route decision
- Onboarding: first-use introduction
- Login: Google sign-in only
- Home: daily dashboard of habits and progress
- Add Habit: create a new habit with icon, color, frequency, plant style
- Habit Detail: complete a habit and see history/progress
- Stats: weekly strip plus 30-day completion chart
- Store: spend water drops on unlockable plants
- Social:
  - local preview mode without sign-in
  - Firebase-backed leaderboard/following/nudges with sign-in
- Profile:
  - edit display name/avatar
  - copy Firebase UID
  - toggle settings
  - logout

## 15. Short whole-system description

HabitSeed is a local-first habit tracker that turns daily completions into a garden-growth loop. The private core of the system is Room: users, habits, logs, settings, store purchases, and mock social data all live locally. Google sign-in and Firestore are optional layers used to identify the user and publish only a limited public garden summary for leaderboards, friend following, and nudges. The UI is built in Compose, the app state is managed with view models and flows, and repository classes isolate the screen layer from Room and Firebase details.
