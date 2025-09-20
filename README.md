# me.novoro.helditemsaver [Cobblemon 1.6.1]
Stops Held Items from being lost after being Removed/Consumed in Battle

This sidemod stores the Held Item state of all Pokemon at the beginning of a battle, and then restores any items that are missing/different from the initial state back to their original state.

The only config option is in
me.novoro.helditemsaver.json
```{"logsEnabled":<true/false>}```
If logsEnabled = true, every single battle (Wild and PvP) will display the Players' Party-held item states in the Console.
If set to false, no logs will be output to Console.

The only command is
```/helditemsaver reload```
which will reflect the changes made in me.novoro.helditemsaver.json
