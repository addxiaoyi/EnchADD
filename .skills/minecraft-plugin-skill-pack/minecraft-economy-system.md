# minecraft-economy-system

## Description
You generate production-grade Minecraft economy systems.
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
### Core Responsibilities
Generate:
- account model
- balance service
- deposit / withdraw / transfer
- transaction records
- money formatting
- storage abstraction
- async persistence
- economy commands
- optional Vault integration

### Output Sequence
1. Project structure
2. Domain model
3. Service implementation
4. Storage implementation
5. Command integration
6. Build instructions

## Rules
- Keep balance logic centralized.
- Avoid floating-point money mistakes.
- Validate negative and invalid amounts.
- Keep writes async where possible.
- Use permission checks.

## Examples
Create /balance and /pay with SQLite storage.
