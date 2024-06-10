export type OnSpeechResultEventPayload = {
  value: string[];
};
export type OnSpeechErrorEventPayload = {
  cause: string;
};
export enum ReactEvents {
  onSpeechStart = "onSpeechStart",
  onSpeechResult = "onSpeechResult",
  onSpeechEnd = "onSpeechEnd",
  onSpeechError = "onSpeechError",
}
