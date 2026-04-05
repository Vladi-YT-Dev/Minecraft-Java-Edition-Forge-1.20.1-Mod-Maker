# AppImage Creation Plan for Minecraft Mod Maker

This plan describes how to convert the current Java Maven project into a fully self-contained Linux AppImage.

## User Review Required

> [!IMPORTANT]
> The final AppImage will include a custom Java Runtime Environment (JRE) to ensure it runs on systems without Java installed. This will increase the file size to approximately 40-70 MB.

> [!NOTE]
> We will generate a custom icon for the application using local tools or image generation if needed.

## Proposed Changes

We will create a multi-step build process that automates the creation of the AppImage.

### Build and Package

#### [NEW] `package_appimage.sh`
A shell script that will cover:
1.  Verifying development tools (`mvn`, `java`, `jlink`, `jdeps`).
2.  Building the application fat JAR using `mvn clean package`.
3.  Determining custom JRE requirements (needed modules like `java.desktop`).
4.  Creating a custom, stripped-down JRE for Linux using `jlink`.
5.  Constructing the `AppDir` directory structure:
    *   `AppDir/usr/bin/` (Executable wrapper)
    *   `AppDir/usr/lib/` (JAR and JRE)
    *   `AppDir/usr/share/applications/` (.desktop file)
    *   `AppDir/usr/share/icons/` (App icon)
6.  Generating the `AppRun` entry point and the desktop metadata.
7.  Downloading and using `appimagetool` to bundle everything into a single `.AppImage` file.

#### [NEW] `minecraft_mod_maker.desktop`
Standard Linux desktop entry for the application.

### Resources

- **Icon**: We'll create or select a suitable Minecraft-themed icon for the AppImage.
- **JRE**: Custom-built using `jlink` to keep the footprint small.

## Open Questions

- Does the user have a specific icon or logo they want to use for the AppImage? Or should I generate a new one?
- Should the AppImage be built for `x86_64` (the current architecture)?

## Verification Plan

### Automated Tests
- Run the `package_appimage.sh` script and check for successful exit.
- Use `appimagetool --validate` if possible.

### Manual Verification
- Run the generated `.AppImage` file on the terminal and verify the UI launches correctly.
- Verify that the application functions (creating mods, fetching icons) inside the AppImage environment.
