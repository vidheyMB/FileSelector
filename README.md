# FileSelector
FileSelector basically gives option to capture image or select from gallery and also select other formated files also like MS-office, MS-Excel, PPT and etc..

Output(Image or File) will be : Base64String, Bytes, file name, file extension and Uri.  

Library for Java and Kotlin both.
[![](https://jitpack.io/v/vidheyMB/FileSelector.svg)](https://jitpack.io/#vidheyMB/FileSelector)


## Installation

```bash

allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

```
in build.gradle (Project)


```bash

implementation 'com.github.vidheyMB:FileSelector:v0.0.6'

```
in build.gradle (Module)

## kotlin 

```kotlin

FileSelector.
                requiredFileTypes(FileType.ALL) // Optional
                .open(this, object : FileSelectorCallBack {
                override fun onResponse(fileSelectorData: FileSelectorData) {
                   fileSelectorData.uri  // image or file uri
                   fileSelectorData.responseInBase64 // image or file in base64 string fromat
                   fileSelectorData.fileName  // image or file name
                   fileSelectorData.extension  // image or file extension(format)
                   fileSelectorData.bytes      // image or file in bytes
                   fileSelectorData.imageBitmap  // only image in bitmap
                   fileSelectorData.thumbnail   // image as thumbnail and file thumbnail as its formats
                }
            })

```

## java 

```java

 FileSelector.INSTANCE.open(getActivity(), new FileSelectorCallBack() {
            @Override
            public void onResponse(@NotNull FileSelectorData fileSelectorData) {

            }
        });

```

in MainActivity.kt 

