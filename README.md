# Least Resistance GPS (LRGPS)
I built this GPS because I prefer driving on smoother, "lower resistance" roads over saving a few seconds on stressful ones. Since I built it for personal use, I decided to make it open-source for others to enjoy.
I plan to make this presentable and release it on the play store when I finish it.

## How it works
Instead of looking strictly at travel time, LRGPS checks road friction, accounting for traffic obstacles (like traffic lights, crossings, intersections, stop signs...), and road surface materials (e.g. asphalt vs unpaved roads), to calculate an easier route to destination.
Classic "fastest path" and "shortest path" routing strategies are also available, just in case.

## Modules
### core (complete)
This module manages the actual routing for the path we want.
Features an OpenStreetMap .osm parser

### geocoding (0%)
Will handle offline/online map downloads and address lookup, passing coordinate bounds directly to <b>core</b>.

### ui-desktop (1%)
This module will allow you to have a visual interface on desktop (through JavaFX). \
For the map itself, I'll use [Mapsforge](https://github.com/mapsforge/mapsforge"). \
I don't plan on wasting too much time on this, since the target destination is on mobile

### ui-android (0%)
This module will make the whole graphical interface on android (through Android native libraries). \
As for the desktop ui, I'll use I'll use [Mapsforge](https://github.com/mapsforge/mapsforge") to display the map itself. \
I'm no designer, but I'll try making the UI acceptable for distribution.

