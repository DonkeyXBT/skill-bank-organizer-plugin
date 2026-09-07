# Skill Bank Organizer

A RuneLite plugin that scans your bank and splits it into **one page per skill**.

Each page shows:

- The best tools you already have (and what’s next)
- Training methods and the supplies those methods need
- Every matching stack currently in the bank
- A **rebuild order** — drag items into that order by hand. The plugin never moves your bank for you.
- **Right-click → Send to [skill]** on a bank item. That opens the matching page and writes a Bank Tag (`#woodcutting`, `#raids`, …). Jagex does not allow plugins to drag items; you still move the stack yourself.
- A **Raids** page for CoX / ToB / ToA kits.

## Install

1. In RuneLite, click the wrench to open **Configuration**.
2. Open **Plugin Hub** at the bottom of the plugin list.
3. Search for **Skill Bank Organizer** and click **Install**.
4. Enable it, then open your bank. Pages fill from the live container.
5. Right-click any stack -> **Send to Woodcutting** (or Raids, Magic, ...). That tags it and opens the page. Drag it onto the tab yourself.

Suggested bank tabs are listed on the [docs site](https://donkeyxbt.github.io/skill-bank-organizer/) (Melee, Ranged, Magic, Prayer, ...). Put a placeholder gap between skills so the layout stays put.

## Plugin Hub

This repository is the plugin-hub source for **Skill Bank Organizer**. It follows the
[example-plugin](https://github.com/runelite/example-plugin) layout, with
`runelite-plugin.properties` at the root as the hub requires.

Development happens in [DonkeyXBT/skill-bank-organizer](https://github.com/DonkeyXBT/skill-bank-organizer),
which also hosts the [docs site](https://donkeyxbt.github.io/skill-bank-organizer/);
the Java source there is mirrored into this repository for submission.

## Notes

- Items can appear on more than one skill (logs sit on Woodcutting, Firemaking and Fletching).
- Unmatched stacks land on **Uncategorized** (tab 9).
- Nothing is automated. No clicking, no moving items — information only.
