for /R C:\workspace\tts\tts-gdx-game-android\assets\3d %%G in (*.fbx) do (
  fbx-conv-win32 -f -o G3DJ %%~nG.fbx %%~nG.g3dj
)

pause