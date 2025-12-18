# Black Hole Item Movement

A Minecraft NeoForge mod that adds black hole portals for instant item transportation and storage.

Idea originally requested on the Minecraft Forums in 2015: [Here](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/requests-ideas-for-mods/2381335-black-hole-mod-request)

## Features

- **Black Hole Item**: Create linked portal pairs to instantly move items between locations
- **Black Hole Wand**: Reusable version of the black hole item for unlimited portal creation
- **Black Hole Chest**: Portable chest with animated lid that can be placed anywhere

## Recipes

- **Black Hole Item**: 8 Ender Pearls surrounding 1 Obsidian
- **Black Hole Wand**: 1 Black Hole Item + 1 Stick (diagonal pattern)
- **Black Hole Chest**: 8 Black Hole Items surrounding 1 Chest

## Installation

1. Download the latest `.jar` file from the [Releases](../../releases) page
2. Place it in your Minecraft `mods` folder
3. Requires NeoForge for Minecraft 1.21.1

## Development Setup

This mod uses the Minecraft NeoForge development environment.

### Prerequisites
- JDK 21
- Gradle (included via wrapper)

### Setup for Eclipse
1. Run `./gradlew genEclipseRuns`
2. Open Eclipse → Import → Existing Gradle Project → Select folder

### Setup for IntelliJ IDEA
1. Open IDEA and import the project
2. Select `build.gradle` and import
3. Run `./gradlew genIntellijRuns`
4. Refresh the Gradle Project if needed

### Useful Commands
- `./gradlew build` - Build the mod
- `./gradlew runClient` - Run Minecraft with the mod
- `./gradlew --refresh-dependencies` - Refresh dependencies
- `./gradlew clean` - Clean build artifacts

## Resources

- [NeoForge Documentation](https://docs.neoforged.net/)
- [NeoForge Discord](https://discord.neoforged.net/)
- [Mojang Mapping License](https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md)
