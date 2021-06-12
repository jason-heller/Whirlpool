# Whirlpool
Open source cross-platform .sm file editor intended to replace ArrowVortex, featuring drag-and-drop functionality and built-in CMOD. Still a work in progress..

# Features
* Drag and drop support for audio, images (banner, cd title, etc), and offset/bpm/preview tags<br>
* Single-window based structure, everything can be done within one window.<br>
* Support for PNG/JPEG/BMP for images, WAV/OGG/MP3 for audio.<br>
* Exporting .sm files<br>

Upcomming features:<br>
* Support for multiple BPM changes (partially implemented)<br>
* Loading sm/ssc files<br>
* Wrapping all media into a single directory on save<br>
* Audio trimming (shortening the audio file to start at the offset)<br>
* Support for adding LUA scripts to files<br>
* Support for holds/rolls/mines (as well as drag and drop for them)<br>
* Adjustable scroll speed<br>
* Better visibility to text when a background is added

# Screenshots

![alt text](https://github.com/jheller9/Whirlpool/blob/master/whirlpool1.png?raw=true)
![alt text](https://github.com/jheller9/Whirlpool/blob/master/whirlpool2.png?raw=true)

# Dependencies
- [LWJGL3] https://www.lwjgl.org/
- [JOML] https://github.com/JOML-CI/JOML (Can be downloaded with LWJGL)
- [NanoVG] https://github.com/memononen/nanovg (Can be downloaded with LWJGL)
- [TinyFG] https://github.com/native-toolkit/tinyfiledialogs (Can be downloaded with LWJGL)
- [LWJGUI] https://github.com/orange451/LWJGUI
