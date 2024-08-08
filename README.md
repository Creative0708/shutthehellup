# Shut the Hell Up!

_A Minecraft mod for bodging away packet-related disconnects_

Exactly what it says on the tin. This adds logic in `net.minecraft.network.Connection` that ignores any
exceptions thrown due to packet decoding errors. This will prevent players from being disconnected when packet-handling
code throws an error.

This was made for a private playthrough where players were being disconnected for a sound packet issue:

```
Can't find id for 'net.minecraft.sounds.SoundEvent@xxxxxxxx' in map Registry[ResourceKey[minecraft:root / minecraft:sound_event] (Experimental)]
```

I cannot stress enough how much of a bad idea it is to include this mod in your modpack. If you have the time, find the
root cause and report it to the respective mod authors. Sweeping errors under the rug like this
is a great way to destabilize your modpack.

That said, if binary searching your modlist takes too long or if you just can't be bothered to do it the right way, this
mod might just be for you.

Good luck and godspeed.
