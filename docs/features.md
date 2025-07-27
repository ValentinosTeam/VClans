# VClans technical documentation of all features

This document provides a comprehensive overview of the features available in VClans, including their functionality and
usage.

---

## Features Overview

- **Clan System**: The code feature of the plugin. Allows players to create clans, manage members, manage their
  permissions,
  and more.
- **Clan Chat**: A private clan chat for clan members to communicate.
- **Clan Ranks**: The ability to create and manage ranks within a clan, allowing for different permissions and roles.
- **Chunk Claims**: The ability to claim chunks for a clan, protecting them from other players.
- **Chunk Radar**: A hud that shows claimed chunks in the player's vicinity.
- **BlueMap Support**: Integration with BlueMap to visualize clan claims on the map.
- **WorldGuard Support**: Adds a new region flag to prevent chunk claims in WorldGuard regions.
- **Vault Economy**: Uses Vault for economy support, allowing clans to charge for chunk claims and other features.
- **EssentialsXChat Prefixes**: Integration with EssentialsXChat to add clan prefixes to player names in chat.

---

## Feature Details

### Clan System

The core feature of VClans. It allows any player to create a clan, manage it's members and claim chunks for the clan.
Clans have many attributes:

- **Id**: A unique identifier for the clan that is set at clan creation `/clan create <id>` and cannot be changed. The
  id has to be unique and has many restrictions such as no spaces, no special characters, and a maximum length of 16
  characters. The clan id is used to reference the clan, because the name of the clan can be challenging to use in
  commands. (i.e. `/clan join <id>` instead of /clan join <name>)
- **Name**: The name of the clan, which can be set with `/clan rename <name>`. The name has a configurable maximum
  length of 32 characters and can contain spaces and minecraft formatting codes for colors. (`/clan rename &4My Clan` to
  name the clan "My Clan" with red color)
- **Color**: Color of the clans territory, used in the chunk radar and BlueMap. The color can be set with
  `/clan color <r> <g> <b>`. The color is represented as RGB values, each ranging from 0 to 255.
- **Tier**: The tier of the clan, which is used to determine the maximum number of members and chunks that can be
  claimed by the clan, but also custom effects like regeneration for clan members and glow effects for enemies in the
  claimed chunks.
- **Prefix**: The prefix of the clan, which is used in the chat and can be set with
  `/clan prefix <prefix>`. The prefix can contain minecraft formatting codes for colors. (`/clan prefix &4MC` to
  set the prefix to "[MC]" with red color)
- **Owner**: The player who created the clan. The owner has all permissions in the clan and can manage members, ranks,
  and chunks. No one else can take the ownership of the clan.
- **Members**: A map of members to a ClanMember object which contains all the relevant information about the member like
  the clan join date and their rank.
- **Ranks**: A map of ranks to a ClanRank object which contains the name of the rank, the permissions (`canClaim`,
  `canKick`, etc) of the rank, and it's priority number. The priority number is used to determine the order of ranks in
  the clan. For example: Members with the rank that has a permission to kick other members, cannot kick members with a
  higher priority rank than them.
- **Invites**: A map of players to ClanInvite objects which track the invitation status of players to the clan. _Does
  not
  persist between server restarts._
- **Chunks**: A set of ClanChunks which contains all the chunks claimed by the clan. Each chunk has a size of 16x16 and
  spans the whole height of the world. The chunks offer protection against other players, meaning that
  only clan members can build and interact with blocks in the claimed chunks.

Most of these attributes can be viewed by the server administrators in the `clans.json` file in the plugins folder.

### Commands

The commands are designed to be intuitive and easy to use, with most commands having a dynamic suggestion system that
doesn't suggest subcommands that the player does not have permission to use or does not suggest options that do not make
sense to suggest (`/clan invite <player>` will only suggest players that are **not** currently in a clan).
Also most commands have a permission node tied to them that the owner can use to restrict access to certain commands.

#### `/clan`

- `/clan`: used as is just displays the current clan of the player.
    - `/clan help`: displays a list of all available subcommands and their usage.
    - `/clan create <id>`: creates a new clan with the given id. The id has to be unique and has many restrictions
      such as no spaces, no special characters, and a maximum length of 16 characters.
    - `/clan disband`: disbands the clan, removing all members and chunks. Only the owner can disband the clan.
    - `/clan rename <name>`: renames the clan to the given name. The name has a configurable maximum length of
      characters
      and
      can contain spaces and minecraft formatting codes for colors.
    - `/clan prefix <prefix>`: sets the prefix of the clan to the given prefix. The prefix can contain minecraft
      formatting codes for colors.
    - `/clan color <r> <g> <b>`: sets the color of the clan to the given RGB values. Each value ranges from 0 to 255.
    - `/clan invite <player>`: invites the given player to the clan.
    - `/clan join <id>`: joins the clan with the given id. The player has to be invited to the clan first.
    - `/clan leave`: leaves the clan, removing the player from the clan. Owners cannot leave the clan, they have to
      disband
      it
      instead.
    - `/clan kick <player>`: kicks the given player from the clan.
    - `/clan list <id>`: if a clan id is given, lists all members of the clan with their ranks.
      If no id is given, lists all clans on the server.
    - `/clan upgrade`: upgrades the clan to the next tier if the player running the command can afford it. Tiers are
      configurable in the configs explained further down.
    - `/clan rank <player> <rank>`:
        - if player and rank are given, sets the rank of the player to the given rank.
        - If only player is given, displays the rank information of the player.
        - If ran as is `/clan rank`, shows a GUI to create, edit and delete ranks. See the GUI section bellow for more
          information.
- `/chunk`: used as is just displays information about the chunk the player is currently in.
    - `/chunk claim`: claims the chunk for the clan the player is currently in. The player has to be in a clan and
      have the permission to claim chunks. The chunk has to be unclaimed, far enough away from other claimed chunks or
      WorldGuard protected regions with the custom flag (more info down below), and has to be adjacent to another
      claimed chunk.
    - `/chunk unclaim`: unclaims the chunk for the clan the player is currently in. The player has to be in a clan and
      have the permission to unclaim chunks. The chunk can not be unclaimed if it will split the claimed chunks into 2
      or more distinct territories.
    - `/chunk radar`: toggles the chunk radar on or off. The chunk radar shows all claimed chunks in the player's
      vicinity in a 5x5 grid with the north pointing upward.
- `/confirm`: if a command requires confirmation (see the configuration section below), the player will be prompted to
  run `/confirm` within a certain time limit to confirm the command.
- `/cancel`: if a command requires confirmation, the player can run `/cancel` to cancel the command.
