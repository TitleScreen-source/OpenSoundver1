# Showcase / Reels контур

Этот контур хранит эталонные визуальные демонстрации AUDMORA. Это не временный код и не черновой UI: такие сцены показывают, к какому уровню атмосферы, синхронизации с музыкой и визуального давления должно стремиться основное приложение.

## Текущий кейс

- Showcase-трек: `I Feel Sick` / `Subaru Natsuki`
- Идентификатор: `AudMoraSeedTrackIds.RezeroShowcase`
- Визуальный режим: `TrackVisualMode.ShowcaseReels`
- Аудио: `app/src/main/res/raw/rezero_showcase.mp3`
- Мини-плеер reels: `app/src/main/java/showcase/ReelsShowcaseMiniPlayer.kt`
- Showcase-профиль: `app/src/main/java/showcase/AudmoraShowcaseProfileScreen.kt`
- Основные ассеты: `rezero_subaru.png`, `audmora_showcase_header.jpg`, `audmora_showcase_avatar.jpg`

## Как подключено

- `AudMoraCatalogRepository` задает треку стабильный `TrackId`, локальный `audioResId` и `TrackVisualMode.ShowcaseReels`.
- `MainActivity` передает `usesShowcaseVisuals` в профильный экран как `showcaseMode`.
- `MiniPlayer` заменяет обычный `AtmosphereMiniPlayerContent` на `ReelsShowcaseMiniPlayer`, если `track.usesShowcaseVisuals`.
- `ArtistProfileScreen` при `showcaseMode` делегирует экран в `AudmoraShowcaseProfileScreen`.

## Правила для следующих кейсов

- Новые reels/showcase-сцены держать в пакете `com.opensound.app.showcase`.
- Не смешивать эталонные сцены с обычными экранами приложения, плеером и будущей бизнес-логикой.
- Хранить тайминги, cue-тексты, визуальные эффекты и связанные ассеты рядом по смыслу.
- Если эффект выглядит сложным, но задает нужный уровень качества, сохранять его как референс и упрощать только осознанно.
- Для каждого нового showcase-кейса явно указывать точку включения через `TrackId`, `TrackVisualMode` или отдельную showcase-конфигурацию.
