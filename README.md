Cubic Timer is a fork project from Twisty Timer by Ari Neto that support external stack timer.
I really appreciate Ariovaldo Neto that making original Twisty Timer.
https://github.com/aricneto/TwistyTimer

Original description of Twisty Timer is following.

> Twisty Timer is a Material Design twisty puzzle timer for Android. It uses the TNoodle library to generate scramble sequences for all current official speedsolving puzzles.  
> If you would like to add a new feature or fix something, just send a pull request.  
> Special thanks to Prisma Puzzle Timer, TNoodle and PlusTimer for being my inspirations to create this project :).

# Smart timer support
## How to connect external smart timer
Tap the Bluetooth icon and the app will scan for Bluetooth devices.
Tap the found device name/address to connect to Smart Timer.

## Supported smart timer
- Gan Smart Timer
- Gan Halo Timer Smart
- QiYi Smart Timer
- QiYi Timer Bluetooth Adapter

# Stack timer support
## How to connect external stack timer
Prepare RS232C to USB convert cable (IMPORTANT!! IT IS NOT AUDIO CABLE used by csTimer).
I recommend RS232 3.5mm Audio Jack Serial Adapter Cable used for the Intel Galileo Gen1 board. But the operation is not guaranteed.
Output voltage level of G4 stack timer is lower than the specification of RS232C. Therefore, sometimes there are compatibility problems.

## Supported stack timer
- Speed Stacks Pro Timer G5
- Speed Stacks Pro Timer G4
- Speed Stacks Pro Timer G3 (not confirmed, maybe)
- YuXin Timer V2
- Gan Halo Timer Smart
- QiYi Smart Timer

## Supported RS232C to USB converter device
It depends on usb-serial-for-android.
I confirmed USB-RS232 cable using FT232R.
(https://www.amazon.co.jp/gp/product/B08F7GVJH3/)

# About translation
[![Crowdin](https://badges.crowdin.net/cubic-timer/localized.svg)](https://crowdin.com/project/cubic-timer)

Cubic Timer uses crowdin for multilingual translation. Your cooperation in translation is greatly appreciated.
https://crowdin.com/project/cubic-timer

# How to fork this project
This project is licensed by GPL. So, you can fork this project and re-distribute/publish it as another application.
But I would like you to change application name, icon and ID to distinguish it from Cubic Timer.

# License (GNU GPL v3)

    Copyright (C) 2016  Ariovaldo Neto
    Copyright (C) 2023  hatoya

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
