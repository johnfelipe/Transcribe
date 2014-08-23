Transcribe
==========

Audio transcription software taking advantage of visual guidance and segmented work.<br/>
Documentation eventually.

# Example
<img src="http://i.imgur.com/B2xA9Cj.png"></img>

# Why?
- Much focus on audio transcription is spent on improving automated audio transcription. <br/>

- Current efforts of automated transcription are primarily stable in the area of identity-less transcription. That is, computers are not aware of who is speaking, they are mostly aware of what is being said (and sometimes that is not the case either)

- Meanwhile, manual audio transcription is still done with a textbox and a media player capable of a couple elementary functions like time scale or pause/unpause.<br/>

## So how is this an improvement?
- Transcribe <strong>displays an Audacity-style waveform</strong>, giving the transcriber a handy visual method of working with audio, rather than maintaining a mental picture of the media timeline. This is particularly useful when media files have long empty segments: the waveform clearly denotes empty sound/

- Transcribe eliminates the notion of working on the 'entirety of the audio', and tries to <strong>separate work into segments</strong>. A segment can be described as simply a section of the audio the transcriber has decided contains meaningful speech. When it's time to submit the final product (the transcript!), the software puts all these segments together with a <strong>highly configurable compiler</strong>. 

- Transcribe features a <strong>type-based</strong> transcription system. Segments can be defined as of a certain type (such as Interviewer, Client #1, Client #2, Q, A, etc.) and at compile-time will be displayed as such.

- If you need <strong>timestamps</strong>, Transcribe handles that for you. Conventional audio transcription software tries to simplify timestamps with a hotkey inputing the current media time. Why should the transcriber even bother doing manual timestamps, modern computers are more than capable of that themselves.