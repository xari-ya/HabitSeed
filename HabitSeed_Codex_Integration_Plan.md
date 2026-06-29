# HabitSeed Codex Integration Plan

## Purpose

Use this file as the **single development brief for Codex**. The goal is not to rebuild the Android app from scratch. The goal is to upgrade the existing `HabitSeed` Kotlin/Jetpack Compose project so it matches the provided 10-frame HabitSeed concept and UI reference screens, while keeping the current Room + Hilt + Compose architecture clean and testable.

Work **one step at a time**. After every step, build and test. Do not continue to the next step if the current step breaks compilation or core navigation.

---

## Current Project Reality Check

The current `HabitSeed.zip` project is an Android app using:

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt dependency injection
- Room database
- Local-only data layer

Current implemented screens:

- Home / My Garden
- Add Habit full screen
- Habit Detail
- Shop / Market

Current database entities:

- `UserEntity`
- `HabitEntity`
- `HabitLogEntity`
- `ShopItemEntity`

Current major gaps against the requested concept:

- No onboarding flow.
- No login/sign-up screen.
- No proper splash branding colors/assets.
- Bottom navigation is incomplete: only Home and Shop exist.
- Add Habit is currently a full page, not the requested slide-up modal.
- Stats screen is missing.
- Social/Friends screen is missing.
- Profile/Settings screen is missing.
- The design system colors are close but not exact.
- `PlantVisualizer` uses a placeholder Android gallery icon instead of the supplied plant/seed assets.
- Database lacks unique daily completion protection, proper settings, purchases, friends, and richer habit metadata.
- Streak logic is too basic for a real habit tracker.

Do not ignore these gaps. The assignment is visual/UI-heavy, so a working app that looks generic is not enough.

---

## UI Asset Zip Contents

The second zip contains reference screens and visual assets. Use these as the source of visual truth.

Important folders:

```text
a_flat_vector_seed_illustration_simple_and_elegant._a_white_ellipse_body_with_a/screen.png
```

Use this as the splash/onboarding seed brand image.

```text
a_highly_detailed_unique_succulent_plant_with_thick_translucent_purple_and_teal/screen.png
```

Use this as the first real plant illustration for the habit detail screen and dashboard plant.

```text
frame_2_onboarding_slide_1/screen.png
frame_3a_sign_up/screen.png
frame_4_home_dashboard/screen.png
frame_5_add_habit_modal/screen.png
frame_7_statistics/screen.png
frame_8_seed_store/screen.png
frame_9_social_gardens/screen.png
frame_10_profile_settings/screen.png
habit_detail_modern_succulent/screen.png
botanical_growth_system/DESIGN.md
```

Use these as layout/style references. The screenshots themselves should go in a documentation/reference folder if needed, not inside the app UI unless they are being used only as temporary reference.

Recommended import locations:

```text
app/src/main/res/drawable-nodpi/seed_splash.png
app/src/main/res/drawable-nodpi/plant_succulent.png
app/src/main/res/drawable-nodpi/onboarding_watering_calendar.png
```

If the onboarding watering-can illustration is only present inside the screenshot and not as a standalone asset, create a simple Compose placeholder illustration instead of using the full screenshot as an app asset.

---

## Non-Negotiable Design System

Apply this globally.

| Token | Value | Usage |
|---|---:|---|
| Primary / Forest Green | `#2D6A4F` | Main buttons, active nav, healthy plants |
| Accent / Sunset Orange | `#FF7D00` | FAB, streaks, CTA, completion moments |
| Background / Cream | `#F9F9F6` | App background |
| Surface | `#FFFFFF` | Cards, sheets, inputs |
| Header text / Dark Slate | `#1B4332` | Titles and important text |
| Subtext / Light Grey | `#95A5A6` | Labels, placeholders, helper text |
| Soft error red | `#D76A6A` or similar | Log out / destructive text only |

Typography:

- Prefer Nunito/Nunito Sans/Quicksand if already available locally.
- Do **not** add font files unless they are already included or can be handled cleanly.
- If custom font setup becomes a time sink, use `FontFamily.SansSerif` but keep rounded-friendly sizing and weights.

UI characteristics:

- Cream background everywhere.
- White cards.
- Rounded cards: `20.dp`.
- Rounded inputs/buttons: `16.dp` to `20.dp`.
- Floating action button: Sunset Orange.
- Soft elevation: green-tinted shadows where possible; Material `CardDefaults.cardElevation(6.dp)` is acceptable.
- Avoid harsh borders.
- Filled, rounded icons.

Implementation rule: turn off Material dynamic color by default. Dynamic colors will destroy the exact brand palette.

---

## Target Navigation Map

Implement these routes:

```text
splash
onboarding
login
home
habit_detail/{habitId}
stats
store
social
profile
```

Preferred bottom navigation tabs:

```text
Home | Stats | Store | Social | Profile
```

The original concept mentions Home, Stats, Social, Profile, but the app also requires the Seed Store frame. For a portfolio/demo app, putting Store in the bottom nav is the cleanest way to make all major frames reachable. If a 4-tab nav is strictly required by the lecturer, keep Store reachable from a drops/rewards button in Home and Profile instead.

Navigation rules:

- Splash, Onboarding, and Login must not show bottom navigation.
- Home, Stats, Store, Social, and Profile must show bottom navigation.
- Habit Detail should not show bottom navigation unless the current app design strongly benefits from it.
- Add Habit should become a modal bottom sheet triggered from Home FAB, not a separate full-screen page.

---

# Database Schema Design

## Schema Goals

The database must support:

- Local demo login/profile.
- Habit creation.
- Daily/weekly/custom habit frequency.
- One completion per habit per day.
- Streaks.
- Plant growth level.
- Water drop rewards.
- Seed Store unlocks/purchases.
- Stats screen graphs.
- Mock social/friends screen.
- Settings toggles.

This should remain local Room-first. Do not introduce Firebase unless the assignment explicitly requires real online authentication/social sync.

---

## Proposed Room Schema Version

Upgrade Room database to **version 2** or higher. Since this is still a student/demo app, destructive migration is acceptable during development, but implement the schema cleanly so it is not garbage.

During active development:

```kotlin
.fallbackToDestructiveMigration()
```

Before final submission, either keep destructive migration if no real users exist, or add a formal migration if required.

---

## Tables

### 1. `users`

Single local user for demo mode.

```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    email TEXT,
    avatar_asset_name TEXT,
    joined_at INTEGER NOT NULL,
    water_drops INTEGER NOT NULL DEFAULT 0,
    current_streak INTEGER NOT NULL DEFAULT 0,
    best_streak INTEGER NOT NULL DEFAULT 0,
    selected_theme TEXT NOT NULL DEFAULT 'forest',
    onboarding_complete INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

Room entity fields:

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "local_user",
    val name: String,
    val email: String? = null,
    val avatarAssetName: String? = null,
    val joinedAt: Long = System.currentTimeMillis(),
    val waterDrops: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val selectedTheme: String = "forest",
    val onboardingComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

Important rename: the current app uses `seeds`. The concept says users spend **water drops**. Rename to `waterDrops` for clarity.

---

### 2. `user_settings`

```sql
CREATE TABLE user_settings (
    user_id TEXT PRIMARY KEY NOT NULL,
    notifications_enabled INTEGER NOT NULL DEFAULT 1,
    reminder_hour INTEGER NOT NULL DEFAULT 8,
    reminder_minute INTEGER NOT NULL DEFAULT 0,
    dark_mode_enabled INTEGER NOT NULL DEFAULT 0,
    sound_enabled INTEGER NOT NULL DEFAULT 1,
    haptics_enabled INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

Room entity:

```kotlin
@Entity(
    tableName = "user_settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserSettingsEntity(
    @PrimaryKey val userId: String = "local_user",
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val darkModeEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)
```

---

### 3. `plant_types`

Plants available by default or through the Seed Store.

```sql
CREATE TABLE plant_types (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    rarity TEXT NOT NULL DEFAULT 'common',
    asset_name TEXT NOT NULL,
    price_drops INTEGER NOT NULL DEFAULT 0,
    is_default INTEGER NOT NULL DEFAULT 0
);
```

Suggested seed data:

| id | name | price | default |
|---|---|---:|---:|
| `succulent` | Succulent | 0 | yes |
| `bonsai` | Bonsai Tree | 300 | no |
| `venus_flytrap` | Venus Flytrap | 450 | no |
| `sakura_bonsai` | Sakura Bonsai | 600 | no |
| `desert_cactus` | Desert Cactus | 250 | no |

---

### 4. `habits`

```sql
CREATE TABLE habits (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    icon_name TEXT NOT NULL,
    color_hex TEXT NOT NULL,
    frequency_type TEXT NOT NULL,
    weekly_days_mask INTEGER,
    target_count INTEGER NOT NULL DEFAULT 1,
    plant_type_id TEXT NOT NULL,
    plant_growth_level INTEGER NOT NULL DEFAULT 0,
    current_streak INTEGER NOT NULL DEFAULT 0,
    best_streak INTEGER NOT NULL DEFAULT 0,
    total_completions INTEGER NOT NULL DEFAULT 0,
    is_archived INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(plant_type_id) REFERENCES plant_types(id)
);

CREATE INDEX index_habits_user_id ON habits(user_id);
CREATE INDEX index_habits_plant_type_id ON habits(plant_type_id);
```

Frequency values:

```text
DAILY
WEEKLY
CUSTOM
```

`weekly_days_mask` can use bit flags:

```text
Sunday = 1
Monday = 2
Tuesday = 4
Wednesday = 8
Thursday = 16
Friday = 32
Saturday = 64
```

For daily habits, `weekly_days_mask` can be `127`.

---

### 5. `habit_logs`

One row per habit per day. This is critical. Without the unique index, the user can water the same habit repeatedly and farm points.

```sql
CREATE TABLE habit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    habit_id INTEGER NOT NULL,
    date_key TEXT NOT NULL,
    completed_at INTEGER,
    status TEXT NOT NULL,
    note TEXT,
    water_drops_awarded INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(habit_id) REFERENCES habits(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX index_habit_logs_habit_date ON habit_logs(habit_id, date_key);
CREATE INDEX index_habit_logs_date_key ON habit_logs(date_key);
```

Status values:

```text
COMPLETED
MISSED
SKIPPED
```

`date_key` format:

```text
yyyy-MM-dd
```

Do not rely only on timestamps for daily uniqueness.

---

### 6. `shop_items`

```sql
CREATE TABLE shop_items (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    price_drops INTEGER NOT NULL,
    item_type TEXT NOT NULL,
    asset_name TEXT NOT NULL,
    linked_plant_type_id TEXT,
    linked_theme_key TEXT,
    is_active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY(linked_plant_type_id) REFERENCES plant_types(id)
);
```

Item types:

```text
PLANT
THEME
ACCESSORY
```

---

### 7. `purchases`

```sql
CREATE TABLE purchases (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    user_id TEXT NOT NULL,
    shop_item_id TEXT NOT NULL,
    price_paid_drops INTEGER NOT NULL,
    purchased_at INTEGER NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(shop_item_id) REFERENCES shop_items(id)
);

CREATE UNIQUE INDEX index_purchases_user_item ON purchases(user_id, shop_item_id);
```

---

### 8. `user_unlocked_plants`

```sql
CREATE TABLE user_unlocked_plants (
    user_id TEXT NOT NULL,
    plant_type_id TEXT NOT NULL,
    unlocked_at INTEGER NOT NULL,
    PRIMARY KEY(user_id, plant_type_id),
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(plant_type_id) REFERENCES plant_types(id)
);
```

---

### 9. `friends`

Local mock data for the portfolio/social frame.

```sql
CREATE TABLE friends (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    avatar_asset_name TEXT,
    current_streak INTEGER NOT NULL DEFAULT 0,
    highest_plant_asset_name TEXT,
    last_active_date_key TEXT,
    is_mock INTEGER NOT NULL DEFAULT 1
);
```

---

### 10. `friend_nudges`

```sql
CREATE TABLE friend_nudges (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    friend_id INTEGER NOT NULL,
    sent_at INTEGER NOT NULL,
    message TEXT,
    FOREIGN KEY(friend_id) REFERENCES friends(id) ON DELETE CASCADE
);
```

---

## DAO Requirements

### Habit DAO must support

- Observe active habits for the current user.
- Insert habit.
- Update habit.
- Archive habit instead of hard deleting.
- Get habit by id.
- Observe logs for date range.
- Observe logs for one habit.

### Habit Log DAO must support

- Insert today completion with `OnConflictStrategy.IGNORE` or `ABORT`.
- Check if habit is completed today by `habitId + dateKey`.
- Count completions by date for Stats graph.
- Count total completions.

### User DAO must support

- Observe local user.
- Create/update local user.
- Add/subtract water drops.
- Mark onboarding complete.
- Update profile/settings.

### Shop DAO must support

- Observe shop items with purchase/unlock status.
- Purchase item transaction.
- Prevent duplicate purchases.
- Prevent negative water drops.

Use `@Transaction` in the repository or DAO for purchase and completion flows.

---

# Implementation Steps for Codex

## Step 0 — Project Hygiene and Baseline Build

### Objective

Make the existing project build cleanly before UI work starts.

### Tasks

1. Open the existing Android project root.
2. Do not rewrite the project from scratch.
3. Add or update `.gitignore` so these are not submitted:

```text
.gradle/
build/
app/build/
local.properties
.idea/workspace.xml
*.iml
```

4. Keep the current package name: `com.habitseed.app`.
5. Check Gradle sync.
6. Run baseline commands:

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
```

### Acceptance Criteria

- App compiles before major changes.
- No generated build folders are required for submission.
- No local machine path such as `C:\Users\Sarith\...` is committed as project configuration.

### Codex Prompt

```text
You are working on the existing HabitSeed Android project. First, do project hygiene only. Do not redesign UI yet. Add/update .gitignore, verify Gradle files, preserve the current package, and make the project compile with ./gradlew clean assembleDebug and ./gradlew testDebugUnitTest. If anything fails, fix only the minimal cause. Report changed files and test results.
```

---

## Step 1 — Import Assets and Create Asset References

### Objective

Bring the seed/plant assets into Android resources without polluting app UI with full reference screenshots.

### Tasks

1. Create:

```text
app/src/main/res/drawable-nodpi/
```

2. Copy/rename standalone visual assets:

```text
seed_splash.png
plant_succulent.png
```

3. If useful for documentation, place reference screenshots under:

```text
docs/ui-reference/
```

4. Do not use entire phone screenshots as UI images inside screens.
5. Update `PlantVisualizer` to use real drawable assets.
6. Add safe fallback mapping:

```kotlin
succulent -> R.drawable.plant_succulent
unknown -> R.drawable.plant_succulent
```

### Acceptance Criteria

- `PlantVisualizer` no longer uses `android.R.drawable.ic_menu_gallery`.
- Splash/onboarding can access the seed image resource.
- App builds.

### Tests

```bash
./gradlew assembleDebug
```

Manual:

- Open Home or Habit Detail.
- Confirm a real plant image appears.

### Codex Prompt

```text
Import the supplied HabitSeed seed and succulent assets into drawable-nodpi with clean Android resource names. Update PlantVisualizer to render the real succulent asset with a fallback instead of android.R.drawable.ic_menu_gallery. Do not use full UI screenshots as app UI assets. Build after changes.
```

---

## Step 2 — Apply the Global Design System

### Objective

Make the existing app visually match the HabitSeed concept before adding screens.

### Tasks

1. Update `Color.kt` to exact design colors:

```kotlin
val ForestGreen = Color(0xFF2D6A4F)
val SunsetOrange = Color(0xFFFF7D00)
val Cream = Color(0xFFF9F9F6)
val White = Color(0xFFFFFFFF)
val DarkSlate = Color(0xFF1B4332)
val LightGrey = Color(0xFF95A5A6)
val SoftRed = Color(0xFFD76A6A)
```

2. Update `Theme.kt`:

- Disable dynamic color by default.
- Use Cream as background.
- Use White as surface.
- Use Forest Green as primary.
- Use Sunset Orange as secondary.
- Use Dark Slate as text.

3. Update `Type.kt` with rounded sans-serif style. Use default sans-serif if custom font setup is not ready.
4. Create reusable UI constants if helpful:

```kotlin
object HabitSeedDimens {
    val ScreenPadding = 20.dp
    val CardRadius = 20.dp
    val ButtonRadius = 18.dp
    val ButtonHeight = 56.dp
}
```

5. Standardize card shape/elevation.

### Acceptance Criteria

- App background is Cream.
- Cards are White with rounded corners.
- FAB is Sunset Orange.
- Main buttons use Forest Green unless specifically a CTA/completion action.
- Bottom nav active item uses Forest Green.

### Tests

```bash
./gradlew assembleDebug
```

Manual:

- Home screen no longer looks like default Material colors.
- Existing Home/Add/Detail/Shop screens use consistent colors.

### Codex Prompt

```text
Apply the HabitSeed design system globally. Update Color.kt, Theme.kt, Type.kt, and reusable component styling so the app uses Forest Green #2D6A4F, Sunset Orange #FF7D00, Cream #F9F9F6, White cards, Dark Slate text, rounded shapes, and soft UI. Disable dynamic colors by default. Do not add new feature screens yet. Build after changes.
```

---

## Step 3 — Upgrade Database Schema and Repository Layer

### Objective

Replace the weak demo schema with a schema that can support all requested frames.

### Tasks

1. Update Room database version to `2` or higher.
2. Add/modify entities based on the schema section above:

Required:

```text
UserEntity
UserSettingsEntity
PlantTypeEntity
HabitEntity
HabitLogEntity
ShopItemEntity
PurchaseEntity
UserUnlockedPlantEntity
FriendEntity
FriendNudgeEntity
```

3. Add foreign keys and indices.
4. Add unique index on `HabitLogEntity(habitId, dateKey)`.
5. Rename points from `seeds` to `waterDrops` in user-facing code.
6. Add seed/prepopulate data:

- local user: `Alex`, `alex@example.com`, 1250 drops for demo or lower if testing purchases.
- default plant: Succulent.
- shop plants: Bonsai, Venus Flytrap, Sakura Bonsai, Desert Cactus.
- mock friends: Sam, Maya, Alex/Noah style entries.

7. Add repository methods for:

```text
completeHabit(habitId, dateKey)
purchaseShopItem(userId, shopItemId)
markOnboardingComplete()
updateSettings()
getStatsForLast30Days()
getTodayHabitsWithCompletionStatus()
```

8. Completion must be transactional:

- If already completed today, do nothing.
- If not completed today, insert log, add water drops, update streak, update plant growth, update total completions.

### Acceptance Criteria

- Fresh install creates demo user and seed data.
- Completing the same habit twice on the same day does not add duplicate drops.
- Store purchase cannot create negative water drops.
- Existing screens still load.

### Tests

Add unit/DAO tests where practical:

```text
HabitLogDaoTest_insertDuplicateSameDate_isRejectedOrIgnored
HabitRepositoryTest_completeHabit_awardsDropsOnlyOnce
ShopRepositoryTest_purchase_decrementsDropsAndMarksPurchased
ShopRepositoryTest_purchaseWithoutEnoughDrops_doesNothing
```

Run:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

### Codex Prompt

```text
Upgrade the Room schema to support the full HabitSeed concept. Add user settings, plant types, purchases, unlocked plants, friends, and nudge tables. Add a unique daily completion constraint on habit_logs(habitId, dateKey). Rename user points from seeds to waterDrops. Add seed data for demo user, plants, shop items, and friends. Implement transactional completeHabit and purchaseShopItem flows. Add focused tests for duplicate completion and purchase rules. Build and run tests.
```

---

## Step 4 — Navigation Shell and Bottom Navigation

### Objective

Create the final app navigation structure.

### Tasks

1. Update `Screen.kt` routes:

```kotlin
object Splash : Screen("splash")
object Onboarding : Screen("onboarding")
object Login : Screen("login")
object Home : Screen("home")
object HabitDetail : Screen("habit_detail/{habitId}")
object Stats : Screen("stats")
object Store : Screen("store")
object Social : Screen("social")
object Profile : Screen("profile")
```

2. Update `MainScreen` to show bottom nav only on:

```text
home, stats, store, social, profile
```

3. Bottom nav items:

```text
Home | Stats | Store | Social | Profile
```

4. Use filled rounded icons from Material icons.
5. Active icon/text should be Forest Green.
6. Store route can reuse and rename the current `ShopScreen` initially.
7. Keep Habit Detail back navigation working.

### Acceptance Criteria

- All five bottom tabs are reachable.
- Back stack does not duplicate tabs endlessly.
- No bottom nav on onboarding/login/detail.
- Existing Home and Shop/Store still work.

### Tests

Manual:

- Tap every bottom nav item.
- Open habit detail and return.
- Press Android back from each tab.

Automation if practical:

```text
NavigationTest_bottomTabsNavigateCorrectly
```

### Codex Prompt

```text
Implement the final navigation shell for HabitSeed. Add routes for splash, onboarding, login, home, habit_detail/{habitId}, stats, store, social, and profile. Show bottom navigation only on home/stats/store/social/profile. Add a five-tab bottom nav using the HabitSeed design system. Reuse existing ShopScreen as Store temporarily if needed. Build and manually verify route navigation.
```

---

## Step 5 — Frame 1 Splash Screen

### Objective

Match the concept splash screen.

### UI Requirements

- Full-screen Forest Green background.
- Centered white/glowing seed sprout illustration.
- `HabitSeed` text below in bold white type.
- Minimal, clean, no extra UI.

### Tasks

1. Update `themes.xml` splash background to Forest Green.
2. Use seed asset as splash icon if compatible.
3. Add a Compose `SplashScreen` route if needed for pre-Android fallback or onboarding decision.
4. After a short delay or state check:

```text
if onboardingComplete -> home
else -> onboarding
```

For a student demo, a short in-app splash route is acceptable even if Android system splash already exists.

### Acceptance Criteria

- Launch does not show a white/default splash.
- Brand colors and seed are visible immediately.
- App transitions to onboarding on fresh install.

### Tests

Manual:

- Clear app data.
- Launch app.
- Confirm splash -> onboarding.

### Codex Prompt

```text
Implement the HabitSeed splash experience. Use Forest Green #2D6A4F background, centered white seed sprout asset, and HabitSeed white text. Update Android splash theme and add a Compose splash route if needed to decide onboarding vs home. Fresh install should go to onboarding. Build after changes.
```

---

## Step 6 — Frame 2 Onboarding

### Objective

Create the onboarding value proposition screen.

### UI Requirements

- Cream background.
- Top 60% illustration area.
- Bottom 40% text and CTA.
- Header: `Grow Your Best Self`
- Subtext: `Turn your daily routines into a thriving digital garden.`
- Full-width Forest Green button: `Get Started`
- Carousel dots at bottom.

### Tasks

1. Create `OnboardingScreen.kt` and ViewModel if needed.
2. Use Compose illustration or imported image.
3. On Get Started:

```text
mark onboarding complete if desired
navigate to login
```

For a strict flow, mark complete after successful login. For demo simplicity, mark it when Get Started is tapped.

### Acceptance Criteria

- Looks close to `frame_2_onboarding_slide_1/screen.png`.
- CTA navigates to Login.
- Back navigation does not return to Splash.

### Tests

Manual:

- Fresh install -> Onboarding.
- Tap Get Started -> Login.

Compose test:

```text
onboarding_getStarted_navigatesToLogin
```

### Codex Prompt

```text
Create the HabitSeed onboarding screen matching frame_2_onboarding_slide_1. Cream background, illustration top, title Grow Your Best Self, supporting text, carousel dots, and full-width Forest Green Get Started button. Connect it to login navigation. Build after changes.
```

---

## Step 7 — Frame 3 Login / Sign Up

### Objective

Create a clean local-demo authentication screen.

### UI Requirements

- Cream background.
- Centered white card/form layout.
- Inputs: Email, Password.
- Soft shadows and rounded edges.
- `Forgot Password?` text button.
- Large Forest Green `Log In` button.
- Divider with `OR`.
- Outline buttons:

```text
Continue with Apple
Continue with Google
```

Reference screenshot shows Create Account styling. Either Login or Create Account is acceptable if all concept controls exist.

### Tasks

1. Create `LoginScreen.kt`.
2. Use local/demo auth:

- Accept any non-empty email and password of 4+ characters.
- Update/create local user.
- Navigate to Home.

3. Show validation messages for empty/invalid fields.
4. Social buttons can be mock buttons with Snackbar:

```text
Google sign-in is not connected in this demo.
```

Do not add real Firebase unless required.

### Acceptance Criteria

- User can enter credentials and reach Home.
- Form is visually close to reference.
- Buttons do not crash.
- User name/greeting can be derived from email or default to Alex.

### Tests

Manual:

- Empty submit shows validation.
- Valid local login navigates Home.
- Apple/Google buttons show snackbar or safe no-op.

### Codex Prompt

```text
Create a local-demo Login/Sign Up screen matching frame_3a_sign_up and the HabitSeed concept. Add Email and Password fields, Forgot Password text, Log In button, OR divider, and Apple/Google outline buttons. Validate input locally and navigate to Home after successful login. Do not integrate Firebase. Build and test the login flow.
```

---

## Step 8 — Frame 4 Home Dashboard / Greenhouse

### Objective

Upgrade Home to match the main daily dashboard.

### UI Requirements

- Cream background.
- Top greeting: `Good Morning, Alex!`
- Small avatar top right.
- Large white central card with digital pot/plant and progress percentage.
- Today’s habits list at bottom.
- Pill-shaped white habit cards.
- Sunset Orange FAB with white `+`.
- Bottom nav visible.

### Tasks

1. Rename visual title from `My Garden` to a greeting/dashboard style.
2. Compute daily progress:

```text
completedToday / scheduledToday
```

3. Central dashboard card:

- Plant image.
- Progress percent.
- Small encouragement text.

4. Today habit rows:

- Habit icon.
- Name.
- Status chip/check circle.
- Streak/drops indicator.
- Tap opens Habit Detail.

5. FAB opens Add Habit modal bottom sheet.
6. Empty state should still encourage planting a habit.

### Acceptance Criteria

- Fresh demo home visually matches `frame_4_home_dashboard`.
- Daily progress updates after watering a habit.
- FAB opens modal, not full page.
- Bottom nav works.

### Tests

Manual:

- Login -> Home.
- Add a habit.
- Habit appears in today list.
- Tap habit -> detail.
- Complete habit -> return home -> progress updates.

Compose tests:

```text
home_displaysGreetingAndProgress
home_fabOpensAddHabitSheet
```

### Codex Prompt

```text
Upgrade HomeScreen to match the Greenhouse dashboard reference. Use a greeting, avatar, large white plant/progress card, today's habit list with pill cards, and Sunset Orange FAB. Compute daily progress from today's logs. FAB should open the Add Habit modal bottom sheet. Build and test the flow.
```

---

## Step 9 — Frame 5 Add New Habit Slide-Up Modal

### Objective

Replace the full Add Habit page with a modal bottom sheet.

### UI Requirements

- Background dimmed to 50% black.
- White modal sheet from bottom, around 80% screen height.
- Top input placeholder: `Name your habit...`
- Icon selector grid: 6 rounded colorful icons.
- Frequency toggles:

```text
Daily | Weekly | Custom
```

- Plant/color selector if matching reference.
- Massive Sunset Orange button: `Plant Seed`.

### Tasks

1. Create reusable `AddHabitSheet` composable.
2. Move existing Add Habit ViewModel logic into sheet-compatible state.
3. Fields:

```text
name
iconName
colorHex
frequencyType
weeklyDaysMask optional
plantTypeId
reminder toggle optional
```

4. Save habit and dismiss sheet.
5. Keep `Screen.AddHabit` only if needed for deep link, but the main app must use modal.

### Acceptance Criteria

- Sheet looks close to `frame_5_add_habit_modal`.
- Saving creates a habit.
- Empty habit name disables button or shows validation.
- Sheet dismisses safely.

### Tests

Manual:

- Open sheet.
- Select icon/frequency.
- Save.
- Confirm habit appears on Home.

Compose test:

```text
addHabitSheet_saveHabit_addsHabitToHome
```

### Codex Prompt

```text
Convert Add Habit into a ModalBottomSheet matching frame_5_add_habit_modal. Include name input, six rounded icon choices, frequency toggles Daily/Weekly/Custom, optional reminder toggle, and a large Sunset Orange Plant Seed button. Saving must insert a habit and dismiss the sheet. Build and test habit creation.
```

---

## Step 10 — Frame 6 Habit Detail and Swipe to Water

### Objective

Make Habit Detail the emotional/gamified action screen.

### UI Requirements

- Top half Forest Green or soft green immersive section.
- Large plant illustration for this habit.
- Bottom half Cream/White content area.
- Stats cards: completion rate, streak, total.
- Daily log strip if useful.
- Massive swipeable bottom button: `Swipe to Water`.
- On completion:

```text
button turns Sunset Orange
water/confetti animation or simple celebratory effect
water drops awarded
plant growth level updates
```

### Tasks

1. Update `HabitDetailScreen` layout to match `habit_detail_modern_succulent/screen.png`.
2. Ensure `habitId` is actually read from navigation through `SavedStateHandle`.
3. Improve `SwipeToCompleteSlider`:

- Forest Green track.
- White thumb.
- Sunset Orange completed/progress state.
- Prevent repeated completion.

4. Completion transaction:

- Insert log with today's `dateKey`.
- Award drops only once.
- Update streak and total completions.
- Update growth level:

```text
0 = seed
1 = sprout
2 = small plant
3 = grown plant
4 = bloom
```

Suggested growth rule:

```text
level = min(4, totalCompletions / 5)
```

5. If already completed today, show `Watered Today` state.

### Acceptance Criteria

- Cannot farm drops by swiping repeatedly.
- Detail screen visually matches reference better than current generic screen.
- Back navigation works.
- Home progress updates after completion.

### Tests

Unit:

```text
completeHabit_duplicateSameDate_awardsDropsOnce
plantGrowthLevel_updatesAfterCompletions
```

Manual:

- Add Drink Water.
- Open detail.
- Swipe.
- See success state.
- Try swiping again; no duplicate reward.

### Codex Prompt

```text
Redesign HabitDetailScreen to match the modern succulent reference. Top immersive green/soft-green plant section, bottom stats area, daily log row, and a large Swipe to Water control. Hook swipe to transactional completeHabit so water drops and streak update only once per day. Add a simple celebration animation or snackbar. Build and test duplicate completion prevention.
```

---

## Step 11 — Frame 7 Statistics / The Harvest

### Objective

Add the long-term progress dashboard.

### UI Requirements

- Cream background.
- Header: `Your Harvest` or `Statistics`.
- Horizontal current-week calendar strip.
- Curved line graph in Sunset Orange for last 30 days.
- Semi-transparent orange gradient area under the line.
- Two square cards:

```text
Current Streak: 12 Days
Plants Fully Grown: 4
```

Reference: `frame_7_statistics/screen.png`.

### Tasks

1. Create `StatsScreen.kt` and `StatsViewModel.kt`.
2. Query/computed data:

```text
last 30 days completion percentage
current streak
plants fully grown
overall progress
```

3. Implement line graph using Compose `Canvas` to avoid adding chart dependencies.
4. Create reusable `CalendarStrip` composable.
5. Create stat cards with soft UI.

### Acceptance Criteria

- Stats tab is reachable.
- Graph displays even with empty logs.
- Completing habits changes stats.
- No crash with zero habits.

### Tests

Unit:

```text
StatsCalculator_emptyHabits_returnsZeroState
StatsCalculator_last30DaysCompletionRate_isCorrect
```

Manual:

- Complete a habit.
- Open Stats.
- Confirm stats reflect change.

### Codex Prompt

```text
Add the Statistics/Harvest screen matching frame_7_statistics. Include a week calendar strip, custom Compose Canvas orange curved line graph for last 30 days, and soft stat cards for current streak and fully grown plants. Use real Room log data with safe empty states. Build and test.
```

---

## Step 12 — Frame 8 Seed Store / Rewards

### Objective

Upgrade the existing Shop screen into the Seed Store.

### UI Requirements

- Header: `Seed Store`.
- Drops balance top right, e.g. `💧 1,250 Drops`.
- 2x2 grid of white cards.
- Each card:

```text
plant illustration
plant name
price in drops
Forest Green Unlock button
```

- Purchased items show `Owned` or disabled state.

Reference: `frame_8_seed_store/screen.png`.

### Tasks

1. Rename `ShopScreen` to `StoreScreen`, or create new `StoreScreen` and retire old name.
2. Use grid layout:

```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(2))
```

3. Show user water drops.
4. Purchase transaction:

- If enough drops, subtract drops, mark purchased, unlock plant.
- If not enough, show snackbar.

5. Use supplied succulent image for all plants if other assets are missing, but vary names/colors/backgrounds.

### Acceptance Criteria

- Store looks like a marketplace, not a plain list.
- Purchases persist after app restart.
- Insufficient balance is handled.
- Drops balance updates immediately.

### Tests

Manual:

- Open Store.
- Purchase affordable item.
- Balance decreases.
- Item shows Owned.
- Try expensive item with low balance.

Unit:

```text
purchase_affordableItem_unlocksAndDeducts
purchase_duplicateItem_noDoubleDeduct
purchase_insufficientDrops_noChange
```

### Codex Prompt

```text
Convert the current Shop/Market screen into the HabitSeed Seed Store matching frame_8_seed_store. Use a 2-column grid, drops balance, plant cards, prices, and Forest Green Unlock buttons. Purchases must be transactional and persist. Prevent duplicate purchases and negative drops. Build and test.
```

---

## Step 13 — Frame 9 Social / Friends' Gardens

### Objective

Add a mock accountability/social screen.

### UI Requirements

- Cream background.
- Header: `Friends` or `Friends' Gardens`.
- Toggle top:

```text
Leaderboard | Gardens
```

- List/grid of friends.
- Each row/card:

```text
avatar
friend name
mini plant thumbnail
streak/progress
Sunset Orange Nudge button
```

Reference: `frame_9_social_gardens/screen.png`.

### Tasks

1. Create `SocialScreen.kt` and `SocialViewModel.kt`.
2. Use local mock friends from database.
3. Implement toggle state.
4. `Nudge` button inserts into `friend_nudges` or shows snackbar:

```text
Nudge sent to Sam 🌱
```

5. Do not implement real networking.

### Acceptance Criteria

- Social tab is reachable.
- Friends display from DB seed data.
- Toggle changes view state.
- Nudge gives visible feedback and does not crash.

### Tests

Manual:

- Open Social.
- Switch toggle.
- Tap Nudge.

Compose test:

```text
social_nudgeButton_showsConfirmation
```

### Codex Prompt

```text
Add the Social/Friends Gardens screen matching frame_9_social_gardens. Use local mock friends from Room, a Leaderboard/Gardens toggle, friend cards with avatars/mini plants/streaks, and a Sunset Orange Nudge button that records or confirms a nudge. No real networking. Build and test.
```

---

## Step 14 — Frame 10 Profile & Settings

### Objective

Add profile management/settings screen.

### UI Requirements

- Large circular centered avatar.
- User name and join date below.
- Three white grouped cards:

Block 1 — Account:

```text
Edit Profile
Change Password
```

Block 2 — App:

```text
Notifications toggle
Dark Mode toggle
```

Block 3 — Support:

```text
FAQ
Contact Us
Log Out
```

- Log Out text in soft red.
- Toggle active color Forest Green.

Reference: `frame_10_profile_settings/screen.png`.

### Tasks

1. Create `ProfileScreen.kt` and `ProfileViewModel.kt`.
2. Read user/settings from Room.
3. Implement toggles and persist settings.
4. Edit Profile/Change Password/FAQ/Contact can be mock clickable rows with snackbar.
5. Log Out should navigate to Login and not crash.

For demo mode, logging out can keep local data but clear a simple `isLoggedIn` flag if implemented. Do not delete all habits unless explicitly required.

### Acceptance Criteria

- Profile tab is reachable.
- Toggles persist.
- Log Out returns to Login.
- UI matches grouped white cards with soft design.

### Tests

Manual:

- Toggle notifications.
- Leave screen and return.
- Setting persists.
- Tap Log Out -> Login.

Compose test:

```text
profile_toggleNotifications_persistsState
profile_logout_navigatesToLogin
```

### Codex Prompt

```text
Add the Profile & Settings screen matching frame_10_profile_settings. Show centered avatar, user name, join date, grouped Account/App/Support cards, Forest Green toggles, and soft red Log Out. Persist app settings in Room. Mock nonessential actions with snackbar. Build and test.
```

---

## Step 15 — Visual Polish Pass

### Objective

Make the app look like one coherent portfolio product.

### Tasks

1. Standardize all spacing:

```text
Screen horizontal padding: 20.dp
Card inner padding: 16.dp or 20.dp
Card spacing: 12.dp to 16.dp
```

2. Standardize card shape:

```text
20.dp rounded corners
```

3. Standardize button height:

```text
56.dp
```

4. Use Sunset Orange only for high-energy actions:

- FAB
- Plant Seed
- Nudge
- Completion/swipe success
- Streak accents

5. Ensure dark mode toggle does not break the cream-light portfolio look. If dark mode is not actually implemented fully, keep the toggle as mock or clearly persist but do not force broken colors.
6. Add content descriptions for important images/buttons.
7. Remove unused imports and dead screens.
8. Run lint/build.

### Acceptance Criteria

- No screen looks like default Android template UI.
- All 10 conceptual frames are represented.
- App can be demonstrated in under 2 minutes.
- No broken placeholder gallery icons.

### Tests

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
```

If emulator is available:

```bash
./gradlew connectedDebugAndroidTest
```

Manual full demo:

1. Fresh install.
2. Splash appears.
3. Onboarding appears.
4. Login works.
5. Home dashboard appears.
6. Add habit modal creates habit.
7. Habit detail swipe waters plant.
8. Stats update.
9. Store purchase works.
10. Social nudge works.
11. Profile settings toggles work.
12. Logout returns to Login.

### Codex Prompt

```text
Do a visual and QA polish pass across all HabitSeed screens. Enforce consistent cream background, white rounded cards, Forest Green primary actions, Sunset Orange CTA accents, soft UI spacing/elevation, content descriptions, and no broken placeholders. Run clean build and tests. Fix only real issues; do not redesign from scratch.
```

---

# Test Plan Summary

## Build Tests

Run after every step:

```bash
./gradlew assembleDebug
```

Run after data/repository changes:

```bash
./gradlew testDebugUnitTest
```

Run before final submission:

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
```

If emulator/device is connected:

```bash
./gradlew connectedDebugAndroidTest
```

---

## Unit Tests to Add

### Streak tests

```text
completeFirstDay_setsCurrentStreakToOne
completeConsecutiveDays_incrementsStreak
missedRequiredDay_resetsStreak
completeSameDayTwice_doesNotIncrementTwice
bestStreak_updatesOnlyWhenCurrentExceedsBest
```

### Date tests

```text
dateKey_usesLocalDateFormat
startAndEndOfDay_areCorrectForLocalTimezone
```

### Stats tests

```text
last30Days_emptyLogs_returnsZeroCompletion
last30Days_withLogs_returnsCorrectDailyPercentages
fullyGrownPlants_countsGrowthLevelFourOnly
```

### Store tests

```text
purchaseAffordableItem_deductsDrops
purchaseAffordableItem_unlocksPlant
purchaseDuplicateItem_doesNotDoubleDeduct
purchaseInsufficientDrops_doesNothing
```

---

## DAO / Repository Tests to Add

```text
HabitDao_insertAndObserveHabits
HabitLogDao_uniqueHabitDateConstraintWorks
HabitRepository_completeHabitTransactionWorks
ShopRepository_purchaseTransactionWorks
UserSettingsDao_togglePersists
FriendNudgeDao_insertNudgeWorks
```

---

## Compose UI Tests to Add If Time Allows

```text
Onboarding_GetStarted_ShowsLogin
Login_ValidInput_ShowsHome
Home_Fab_OpensAddHabitSheet
AddHabit_Save_ShowsHabitOnHome
HabitDetail_SwipeToWater_ShowsWateredState
Store_UnlockButton_ChangesToOwned
Social_Nudge_ShowsConfirmation
Profile_Logout_ShowsLogin
```

---

# Final Manual Acceptance Checklist

Use this checklist before submission.

## App Launch

- [ ] Splash screen uses Forest Green background.
- [ ] Seed sprout logo appears.
- [ ] `HabitSeed` text appears.
- [ ] App does not show default white splash.

## Onboarding

- [ ] Cream background.
- [ ] Illustration on top.
- [ ] `Grow Your Best Self` header.
- [ ] Correct subtext.
- [ ] Get Started button works.

## Login

- [ ] Email/password fields look like white soft cards.
- [ ] Validation works.
- [ ] Login navigates to Home.
- [ ] Apple/Google buttons are safe mock actions.

## Home

- [ ] Greeting visible.
- [ ] Avatar visible.
- [ ] Central plant/progress card visible.
- [ ] Today habits visible.
- [ ] Sunset Orange FAB opens Add Habit modal.
- [ ] Bottom nav visible.

## Add Habit

- [ ] Modal slides from bottom.
- [ ] Background dim is visible.
- [ ] Name input works.
- [ ] Icon selector works.
- [ ] Frequency toggles work.
- [ ] Plant Seed button creates habit.

## Habit Detail

- [ ] Correct habit title shown.
- [ ] Real plant illustration shown.
- [ ] Stats cards visible.
- [ ] Swipe to Water works.
- [ ] Duplicate watering is blocked.
- [ ] Drops/streak update.

## Stats

- [ ] Calendar strip visible.
- [ ] Orange graph visible.
- [ ] Empty state does not crash.
- [ ] Cards show streak and grown plants.

## Store

- [ ] Drops balance visible.
- [ ] 2-column grid visible.
- [ ] Unlock button works.
- [ ] Owned state persists.
- [ ] Cannot buy without enough drops.

## Social

- [ ] Friends list/garden view visible.
- [ ] Leaderboard/Gardens toggle works.
- [ ] Nudge button works.

## Profile

- [ ] Avatar/name/join date visible.
- [ ] Account/App/Support groups visible.
- [ ] Toggles persist.
- [ ] Log Out navigates to Login.

## Visual Quality

- [ ] No default placeholder gallery icon.
- [ ] No harsh borders.
- [ ] No inconsistent colors.
- [ ] No broken navigation.
- [ ] App looks like the supplied UI references.

---

# Final Submission Notes

Before zipping/submitting:

1. Clean project:

```bash
./gradlew clean
```

2. Do not include:

```text
.gradle/
build/
app/build/
local.properties
```

3. Include source files, Gradle files, and required resources.
4. Take screenshots of the final 10 frames for report/demo if required.
5. Make sure the app can run on a fresh Android Studio import.

---

# Biggest Risks Codex Must Avoid

1. **Rebuilding the app from scratch.** This wastes time and causes integration errors.
2. **Using full UI screenshots as screen backgrounds.** That is fake UI, not a real app.
3. **Adding Firebase too early.** Local demo auth is enough unless explicitly required.
4. **Ignoring duplicate completion protection.** This breaks the habit/reward logic.
5. **Leaving default Material colors.** The assignment is a UI portfolio piece, so visual match matters.
6. **Breaking navigation while adding screens.** Test nav after every screen.
7. **Making Store/Social/Profile static only.** Mock data is fine, but buttons must respond.
8. **Skipping tests.** Every feature step needs at least a build and a manual test.

---

# First Codex Instruction to Use

Start with Step 0 only. Do not ask Codex to implement all steps in one run. That will waste tokens and increase bugs.

```text
Read HabitSeed_Codex_Integration_Plan.md. Perform Step 0 only: project hygiene and baseline build. Do not redesign UI, do not change the database, and do not add screens yet. Fix only what is needed so the existing project builds cleanly. Report changed files and exact build/test results.
```
