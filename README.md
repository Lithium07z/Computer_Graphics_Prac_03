# 컴퓨터 그래픽스 실습 03 — 3D 장면과 카메라 이동

2023년 1학기 컴퓨터 그래픽스 과제를 정리한 Android 앱입니다. Kotlin과 OpenGL ES 3.0으로 텍스처·조명이 있는 3D 장면을 구성하고, 물체 애니메이션·카메라 조작·간단한 충돌 검사를 결합합니다.

## 주요 기능

- **장면 구성**: 텍스처가 있는 바닥, 육각뿔, 그 위에 놓인 텍스처 큐브를 배치합니다.
- **애니메이션**: 육각뿔과 큐브가 좌우로 왕복 이동하고, 큐브와 광원 방향이 시간에 따라 회전합니다.
- **카메라 조작**: 화면 버튼으로 전진·후진하거나 시선을 좌우로 회전합니다.
- **Arcball 회전**: 화면 드래그로 바닥과 배치된 물체를 함께 회전합니다.
- **간단한 충돌 처리**: 카메라 이동 시 XZ 평면의 경계와 물체 주변 영역을 검사합니다. 움직이는 물체가 카메라 위치에 도달하면 시작 위치로 되돌립니다.

## 조작 방법

| 입력 | 동작 |
| --- | --- |
| `◀` / `▶` | 카메라 시선 좌우 회전 |
| `▲` / `▼` | 카메라 전진·후진 |
| 화면 드래그 | Arcball로 장면 회전 |

충돌 검사는 물체 중심 주변의 단순한 XZ 영역을 사용합니다. 정밀한 메시 충돌이나 물리 엔진을 구현한 프로젝트는 아닙니다.

## 코드 안내

Kotlin 소스는 [`app/src/main/java/kr/ac/hallym/prac03`](app/src/main/java/kr/ac/hallym/prac03)에 있습니다.

- [`MainActivity.kt`](app/src/main/java/kr/ac/hallym/prac03/MainActivity.kt): 화면 구성, 카메라 버튼과 터치 입력 연결
- [`MainGLRenderer.kt`](app/src/main/java/kr/ac/hallym/prac03/MainGLRenderer.kt): 장면 배치, 행렬 계산, 애니메이션, 카메라 이동과 충돌 검사
- [`MyArcball.kt`](app/src/main/java/kr/ac/hallym/prac03/MyArcball.kt): 드래그 입력을 회전 행렬로 변환
- `MyLitCube.kt`, `MyLitTexCube.kt`, `MyLitHexa.kt`, `MyLitTexGround.kt`: 도형별 정점·법선·텍스처 및 조명 속성
- [`app/src/main/assets`](app/src/main/assets): 큐브·육각뿔·바닥의 GLSL 셰이더와 BMP 텍스처
- [`app/src/main/res/layout`](app/src/main/res/layout), [`layout-land`](app/src/main/res/layout-land): 세로·가로 화면 레이아웃

렌더링 흐름은 `MainGLRenderer.onDrawFrame`, 이동 제한은 같은 파일의 `cameraMove`와 `detectCollision`에서 확인할 수 있습니다.

## 실행 방법

1. 저장소를 복제하고 Android Studio에서 루트 폴더를 엽니다.
2. Android SDK Platform 33을 설치하고 Gradle JDK를 11로 설정한 뒤 동기화를 진행합니다. JDK 기준은 [AGP 7.4 호환성 안내](https://developer.android.com/build/releases/agp-7-4-0-release-notes)를 참고하세요.
3. OpenGL ES 3.0을 지원하는 Android API 24 이상 기기 또는 에뮬레이터에서 `app`을 실행합니다.
4. 시작 화면의 화살표 버튼과 드래그로 장면을 살펴봅니다.

| 항목 | 프로젝트 설정 |
| --- | --- |
| Android Gradle Plugin / Gradle Wrapper | 7.4.2 / 7.5 |
| Kotlin | 1.8.0 |
| compileSdk / targetSdk / minSdk | 33 / 33 / 24 |
| Java 호환성 / Kotlin JVM target | 1.8 / 1.8 |

Android SDK와 Gradle 실행용 JDK를 설정한 환경에서는 루트에서 다음 명령으로 디버그 APK를 빌드할 수 있습니다.

```powershell
.\gradlew.bat assembleDebug
```

macOS/Linux에서는 `bash gradlew assembleDebug`를 사용합니다. 결과 경로는 `app/build/outputs/apk/debug/app-debug.apk`입니다.

## 참고

학부 수업 당시의 실습 코드를 보관한 저장소입니다. 포함된 테스트는 기본 템플릿 수준이며, 렌더링·이동·충돌 동작을 검증하는 자동 테스트는 없습니다. 최신 Android 개발 환경으로의 이전이나 기기별 동작 검증은 별도로 필요합니다.
