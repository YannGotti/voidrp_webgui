# Contributing to WebGUI

Thank you for wanting to improve WebGUI!

## What we accept

| Type | Accepted |
|------|---------|
| Bug fixes | ✅ Always welcome |
| Translations | ✅ Always welcome |
| New features | ⚠️ Open an issue first |
| Refactors | ❌ Usually not |

For new features, please open a Feature Request issue and wait for a response before writing code.

## Development setup

```bash
git clone https://github.com/mc-webgui/webgui.git
cd webgui
./gradlew build          # build all MC versions
./gradlew :1.21.11:runClient  # launch game with the mod
```

Requires Java 21. All three supported Minecraft versions build from the same source.

## Adding a new Minecraft version

1. Add one line to `settings.gradle`:
   ```groovy
   version '1.22', '1.22'
   ```
2. Create `versions/1.22/gradle.properties` with MC-specific dependency versions.
3. Add version-conditional code with Stonecutter syntax if the MC API changed:
   ```java
   //? if mc >= 1.22 {
   newMethod();
   //? } else {
   oldMethod();
   //? }
   ```
4. Run `./gradlew build` to verify all versions compile.

## Pull request checklist

- [ ] `./gradlew build` passes (all MC versions)
- [ ] Tested in-game on at least one MC version
- [ ] `CHANGELOG.md` updated under `[Unreleased]`
- [ ] No unrelated formatting changes

## Code style

- Standard Java conventions
- No unused imports
- Keep it simple — this is a client mod

## License

By contributing you agree your code is licensed under [MIT](LICENSE).
