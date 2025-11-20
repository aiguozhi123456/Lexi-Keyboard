package com.brycewg.asrkb.ime

import android.widget.ImageButton
import com.brycewg.asrkb.store.Prefs
import kotlinx.coroutines.CoroutineScope

/**
 * 畅说模式管理器（OSS 占位实现）
 *
 * OSS 版本不提供基于 VAD 的畅说模式，本类仅保留空实现以满足主工程依赖。
 */
class ContinuousTalkManager(
    private val service: AsrKeyboardService,
    private val prefs: Prefs,
    private val asrManager: AsrSessionManager,
    private val actionHandler: KeyboardActionHandler,
    private val serviceScope: CoroutineScope
) {
    fun onPrefsChanged() {
        // OSS：无操作
    }

    fun onImeViewShown() {
        // OSS：无操作
    }

    fun onImeViewHidden() {
        // OSS：无操作
    }

    fun onExtensionToggleRequested() {
        // OSS：无操作
    }

    fun applyExtensionButtonUi(
        btnExt1: ImageButton?, action1: ExtensionButtonAction,
        btnExt2: ImageButton?, action2: ExtensionButtonAction,
        btnExt3: ImageButton?, action3: ExtensionButtonAction,
        btnExt4: ImageButton?, action4: ExtensionButtonAction
    ) {
        // OSS：无操作（不显示畅说模式按钮）
    }

    fun onVendorChanged() {
        // OSS：无操作
    }

    fun onDestroy() {
        // OSS：无操作
    }
}

