# 训练背景音乐素材来源

应用内置的训练背景音乐来自 OpenGameArt，并优先选用页面标注为 `CC0` 的循环音轨。

## 当前内置曲目

| 本地资源 | 曲目名 | 作者 | 来源页 | 许可证 |
| --- | --- | --- | --- | --- |
| `app/src/main/res/raw/bgm_tense_future_loop.ogg` | Tense Future Loop | Glen Mason | https://opengameart.org/content/tense-future-loop | CC0 |
| `app/src/main/res/raw/bgm_project_utopia.ogg` | Project Utopia | Cong Xu | https://opengameart.org/content/project-utopia-seamless-loop | CC0 |
| `app/src/main/res/raw/bgm_spacy_loop.ogg` | Spacy Loop | Electronic Freaker | https://opengameart.org/content/spacyloop | CC0 |

## 体积控制

为避免训练音乐把 APK 体积拉大，仓库内使用重编码后的发行版，而不是保存原始下载文件。

- 输出格式：Ogg Vorbis
- 采样率：22.05 kHz
- 声道：单声道
- 质量：`q=0`

重编码后的内置文件总大小约为 `324 KB`。

原始下载和中间转码文件只作为一次性处理输入使用，不参与应用打包，也不要求保留在仓库里；如需重生成，可根据上面的来源页重新下载并按相同参数转码。
