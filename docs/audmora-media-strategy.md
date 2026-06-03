# AudMora media and size strategy

AudMora should stay a lightweight app shell. Music, user-generated tracks, atmosphere packs, heavy visual scenes, videos, and high-resolution art should not be bundled into the base APK except for small seed/demo content.

## Current size snapshot

Measured during local development:

- Project folder: about 127 MB
- `app/build`: about 95 MB
- `app/src`: about 10.7 MB
- Debug APK: about 23 MB
- Largest bundled source asset: `app/src/main/res/raw/track1.mp3`, about 8 MB

This is normal for the current prototype stage. The project folder includes generated Gradle/Android build artifacts. The installed app size is closer to the APK/build output than to the workspace folder size, and release builds can be optimized separately.

## What should be bundled

Keep only lightweight essentials in the base app:

- app icons and core UI assets
- small seed/demo audio clips only while prototyping
- small placeholder covers and avatars
- showcase reference assets only while they are actively useful for development
- local fallback assets for empty/error states

Do not bundle:

- user-uploaded tracks
- full music catalog
- large atmosphere packs
- generated reels exports
- high-resolution artist media for every profile
- cached files that can be recreated or downloaded

## Production content model

In a real AudMora build, content should be loaded by id:

- `TrackId` identifies the track
- metadata comes from an API/database
- audio comes from streaming or a downloadable media URL
- cover/profile/atmosphere assets are fetched lazily
- local cache stores recently used media
- cache eviction keeps device storage bounded

This means the APK contains the player/editor/app shell, while the catalog and user content live outside the APK.

## Cache policy direction

The app should eventually separate storage into buckets:

- streaming cache: temporary, automatically evicted
- downloaded tracks: user-controlled, visible in settings
- generated atmosphere previews: temporary, can be rebuilt
- user drafts: durable until saved/deleted
- thumbnails: small, aggressively cached

Each bucket should have a size limit and a clear deletion policy.

## Atmosphere growth

Atmospheres should be data-driven. A saved atmosphere should mostly contain:

- layer metadata
- timings
- transforms
- color/style values
- references to assets by id or URL

It should not duplicate heavy source media. If a visual scene needs large assets, store references and cache the actual files separately.

In code, atmosphere saves should go through `AtmosphereRepository` by `TrackId`. The current prototype uses an in-memory implementation, but the repository boundary is intentionally the future handoff point for Room/DataStore/API/cache storage and for storage limits.

## Build-size guardrails

As the app grows:

- avoid adding long audio files to `res/raw`
- compress images before committing
- prefer WebP/AVIF for static app images where Android support is acceptable
- keep generated exports out of the repository
- inspect APK/AAB size before release
- consider dynamic feature modules only if a feature becomes truly optional and heavy

For now, the current size is acceptable because most weight is build output and prototype audio. The architectural goal is to make sure future user content never becomes APK weight.
