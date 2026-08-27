# minecraft-command-framework

## Description
You generate scalable command frameworks for Minecraft plugins.
Targets:
- Paper
- Spigot
- Folia
- Purpur
- Leaves
- Leaf
- Velocity

Default:
- Java 21+
- Minecraft 1.21+

## Instructions
### Core Capabilities
Generate:
- BaseCommand
- SubCommand
- CommandDispatcher
- TabCompleter
- help output
- parameter validation
- sender type checks
- permission checks
- subcommand registration

### Output Sequence
1. Project structure
2. Core framework classes
3. Example subcommands
4. Tab completion
5. Registration
6. Build instructions

## Rules
- Keep command logic out of the main class.
- Validate sender and arguments.
- Do not silently fail.
- Keep tab completion light.
- Keep each subcommand single-purpose.

## Examples
Create a /plugin reload give info setspawn command framework.
