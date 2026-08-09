# Carpet CRS Addition - Rules

## Rule List

### UseV1212ProjectileLogic

Backports 1.21.2 projectile work logic to 1.21.1

- Type: `boolean`
- Default: `false`
- Options: `false`, `true`
- Categories: `CRS`, `Porting`

---

### PearlCanLoadingChunks

Ender pearls can load chunks like in 1.21.2+

- Type: `boolean`
- Default: `false`
- Options: `false`, `true`
- Categories: `CRS`, `Porting`

---

### DragonAlwaysDropsFirstKillXP

Ender dragon always drops 12000 XP like the first kill

- Type: `boolean`
- Default: `false`
- Options: `false`, `true`
- Categories: `CRS`, `Creative`

---

### RemoveExperienceCooldown

Remove experience orb pickup cooldown and allow instant absorption

- Type: `boolean`
- Default: `false`
- Options: `false`, `true`
- Categories: `CRS`, `Creative`, `Experimental`

---

### UseV1216FireworkLogic

Backports 1.21.6 projectile work logic to 1.21.1

- Type: `boolean`
- Default: `false`
- Options: `false`, `true`
- Categories: `CRS`, `Porting`

### optimizedBoat

Optimize ship behavior so that it enters a semi-dormant state after 3 seconds of inactivity, during which it performs detection of monsters boarding and disembarking, and resumes full behavior when a player boards

- Type: `boolean`
- Default: `false`
- Options: `false`, `true`
- Categories: `CRS`, `Optimization`, `Experimental`

---

### optimizedBoatConstraints

Enable semi‑dormant optimization when the boat count within a 256×512 region exceeds the set threshold, a value of 0 disables the regional limit

- Type: `int`
- Default: `1500`
- Options: `1500`, `2000`, `2500`, `3000`
- Categories: `CRS`, `Optimization`, `Experimental`

---

### ItemHighlight

Makes the item highlighted

- Type: `boolean`
- Default: `false`
- Options: `false`, `true`
- Categories: `CRS`, `Creative`