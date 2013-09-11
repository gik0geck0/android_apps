all: build
build:
	ant debug

clean:
	ant clean

install:
	adb install -r bin/TestingApp-debug.apk

log:
	tail -n250 -F logcat.txt | logcat-color
bil: build install log
