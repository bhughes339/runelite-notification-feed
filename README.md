# Notification Feed

Shows a live feed of RuneLite notifications inside the game window. Gone are the days where you miss a notification and wonder what it was!

Any notification that RuneLite sends shows up in the feed, including ones from core plugins (Idle Notifier, Timers, etc.), and hub plugins. This includes notifications that were fired but not shown (e.g. foreground-disabled notifications).

![alt text](image.png)

This plugin is similar to another active plugin, [Notification Panel](https://runelite.net/plugin-hub/show/notification-panel), which works very well. I created this plugin to cater to my preferred "feed" style using the "[Time] Message" format to more closely match the RuneScape UX.

## Usage

- Move the feed by holding <kbd>Alt</kbd> and dragging it. While <kbd>Alt</kbd> is held, the feed stays visible even when it has no notifications.
- Resize the feed's width by holding <kbd>Alt</kbd> and dragging the edges of the box.
- Clear the feed with <kbd>Shift</kbd> + right-click → *Clear*.

## Settings

**Feed**

| Setting | Description |
| --- | --- |
| Max notifications | How many notifications are shown at once |
| Display duration | Seconds each notification stays (0 = until pushed out or cleared) |
| Fade out | Fade notifications out during their last second |
| Newest on top | Put new notifications at the top instead of the bottom |
| Collapse duplicates | Merge repeats of the same notification into one entry with a count |
| Duplicate time limit | Minutes after which a repeat starts a new entry instead of collapsing (0 = no limit) |
| Show when empty | Keep a placeholder visible when there are no notifications |
| Ignored notifications | Wildcard patterns, one per line, e.g. `*is now idle*` |

**Appearance**

| Setting | Description |
| --- | --- |
| Style | Separate boxes per notification, or one panel |
| Timestamp | None, 24-hour, 12-hour (with or without seconds), or relative ("2m ago") |
| Wrap long text | Wrap long notifications, or cut them off with "..." |
| Font | Notification font |
| Text / Timestamp / Background color | Colors |
