# expo-stt

- Unofficial Speech To Text module for Expo which supported iOS and Android
- Forked [anhtuank7c/expo-stt](https://github.com/anhtuank7c/expo-stt)
- Migrated [react-native-voice functionality](https://github.com/react-native-voice/voice) on [anhtuank7c/expo-stt](https://github.com/anhtuank7c/expo-stt)
- 현재 expo-stt는 구글 음성인식 모달이 따로 떠. 이 대신, react-native-voice처럼 내장 마이크 쓰려고 expo module로 만들어진 expo-stt 위에 react-native-voice 코드를 migrate 했어.

# Demo

![Demo speech to text](demo.png "Demo Speech To Text")

### Add the package to your npm dependencies

```
npm install expo-stt
or
yarn add expo-stt
```

Remember, this module doesn't support [Expo Go](https://expo.dev/expo-go).
So for Expo project, you will need to [generates native code](https://docs.expo.dev/workflow/prebuild/#clean) (Bare React Native project can skip this step)

```
npx expo prebuild --clean
```

### Configure for iOS (Bare React Native project only)

Run `npx pod-install` after installing the npm package.

## Add missing permissions for iOS

Add following key to plugins of `app.json` in Expo project
This is an optional, just use in case you want to customize the permission string

```
  "plugins": [
    [
      "expo-stt",
      {
        "microphonePermission": "Allow $(PRODUCT_NAME) to access your microphone",
        "speechRecognitionPermission": "Allow $(PRODUCT_NAME) to access your speech recognition"
      }
    ]
  ]
```

For Bare React Native project, you need to add these key to `Info.plist` in `ios` directory

```
  <key>NSMicrophoneUsageDescription</key>
  <string>Allow $(PRODUCT_NAME) to access your microphone</string>
  <key>NSSpeechRecognitionUsageDescription</key>
  <string>Allow $(PRODUCT_NAME) to access your speech recognition</string>
```

## Usage

Register some listeners

```
  import * as ExpoStt from 'expo-stt';

  useEffect(() => {
    const onSpeechStart = ExpoStt.addOnSpeechStartListener(() => {
      setSpokenText("");
      setError(undefined);
      setRecognizing(true);
    });

    const onSpeechResult = ExpoStt.addOnSpeechResultListener(({ value }) => {
      setSpokenText(value.join());
    });

    const onSpeechError = ExpoStt.addOnSpeechErrorListener(({ cause }) => {
      setError(cause);
      setRecognizing(false);
    });

    const onSpeechEnd = ExpoStt.addOnSpeechEndListener(() => {
      setRecognizing(false);
    });

    return () => {
      onSpeechStart.remove();
      onSpeechResult.remove();
      onSpeechError.remove();
      onSpeechEnd.remove();
    };
  }, []);
```

There are some functions available to call such as:

- ExpoStt.startSpeech()
- ExpoStt.stopSpeech()
- ExpoStt.destroySpeech()
- ExpoStt.requestRecognitionPermission()
- ExpoStt.checkRecognitionPermission()

Take a look into `example/App.tsx` for completed example

# Contributing

Contributions are very welcome! Please refer to guidelines described in the [contributing guide](https://github.com/expo/expo#contributing).
