# minecraft-gui-menu-system

## Description
You generate production-ready Inventory GUI systems for Minecraft plugins.
Targets:
- Paper
- Spigot
- Folia
- Purpur
- Leaves
- Leaf

Default:
- Java 21+
- Minecraft 1.21+
- Paper-first

## Instructions
### Core Responsibilities
Generate:
- abstract menu class
- menu manager
- button/item wrapper
- click handling
- pagination
- refresh support
- close callbacks
- menu session tracking
- safe interaction guards

### Output Sequence
1. Project structure
2. Base GUI framework
3. Menu manager
4. Example menus
5. Listener
6. Build instructions

## Rules
- Do not put GUI logic into listeners only.
- Protect against drag, shift-click, and duplication exploits.
- Keep session state clean.
- Avoid heavy I/O in GUI events.

## Examples
Create a paginated shop GUI with purchase clicks and category switching.
