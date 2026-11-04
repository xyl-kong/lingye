package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageStore

import android.content.Context
import android.content.SharedPreferences

/**
 * Small read/write surface over the workspace preferences so the composable only asks
 * for typed values instead of touching SharedPreferences keys. Kept as an interface so
 * the repository can be unit tested with a map-backed fake.
 */
interface WorkspacePreferenceStore {
    fun string(key: String, fallback: String? = null): String?
    fun boolean(key: String, fallback: Boolean = false): Boolean
    fun int(key: String, fallback: Int): Int
    fun float(key: String, fallback: Float): Float
    fun putString(key: String, value: String)
    fun putBoolean(key: String, value: Boolean)
    fun putInt(key: String, value: Int)
    fun putFloat(key: String, value: Float)

    /** Applies several keys in one editor transaction. */
    fun write(block: (WorkspacePreferenceEditor) -> Unit)
}

interface WorkspacePreferenceEditor {
    fun string(key: String, value: String)
    fun boolean(key: String, value: Boolean)
    fun int(key: String, value: Int)
    fun float(key: String, value: Float)
}

class SharedPreferencesWorkspacePreferences(private val preferences: SharedPreferences) : WorkspacePreferenceStore {
    override fun string(key: String, fallback: String?): String? = preferences.getString(key, fallback)
    override fun boolean(key: String, fallback: Boolean): Boolean = preferences.getBoolean(key, fallback)
    override fun int(key: String, fallback: Int): Int = preferences.getInt(key, fallback)
    override fun float(key: String, fallback: Float): Float = preferences.getFloat(key, fallback)
    override fun putString(key: String, value: String) { preferences.edit().putString(key, value).apply() }
    override fun putBoolean(key: String, value: Boolean) { preferences.edit().putBoolean(key, value).apply() }
    override fun putInt(key: String, value: Int) { preferences.edit().putInt(key, value).apply() }
    override fun putFloat(key: String, value: Float) { preferences.edit().putFloat(key, value).apply() }

    override fun write(block: (WorkspacePreferenceEditor) -> Unit) {
        val editor = preferences.edit()
        block(object : WorkspacePreferenceEditor {
            override fun string(key: String, value: String) { editor.putString(key, value) }
            override fun boolean(key: String, value: Boolean) { editor.putBoolean(key, value) }
            override fun int(key: String, value: Int) { editor.putInt(key, value) }
            override fun float(key: String, value: Float) { editor.putFloat(key, value) }
        })
        editor.apply()
    }
}

/** Persisted canvas tool defaults that survive app restarts. */
data class CanvasToolPreferences(
    val penOnly: Boolean,
    val eraserArea: Boolean,
    val brushKind: String,
    val highlighterColor: Int,
    val highlighterWidth: Float,
    val inkColor: Int,
    val inkWidth: Float
)

/**
 * Workspace data boundary for page loading and session/tool preferences. Page writes go
 * through [PageRepository]; this class owns the read side plus the small preference set
 * the workspace composable uses, so storage details do not spread through the UI.
 */
class WorkspaceRepository(
    private val loadBootstrap: suspend () -> WorkspaceBootstrap,
    private val preferences: WorkspacePreferenceStore
) {
    constructor(store: PageStore, preferences: WorkspacePreferenceStore) : this({ loadWorkspaceBootstrap(store) }, preferences)
    constructor(context: Context, store: PageStore) : this(
        { loadWorkspaceBootstrap(store) },
        SharedPreferencesWorkspacePreferences(context.getSharedPreferences("ink_preferences", Context.MODE_PRIVATE))
    )

    suspend fun load(): WorkspaceBootstrap = loadBootstrap()

    fun readSession(): WorkspaceSession = WorkspaceSessionCodec.decode(
        mapOf(
            WorkspaceSessionCodec.PAGE_ID to preferences.string(WorkspaceSessionCodec.PAGE_ID, ""),
            WorkspaceSessionCodec.VIEW to preferences.string(WorkspaceSessionCodec.VIEW, WorkspaceView.DOCUMENT.storedValue),
            WorkspaceSessionCodec.TOOL to preferences.string(WorkspaceSessionCodec.TOOL, "移动"),
            WorkspaceSessionCodec.SCALE to preferences.string(WorkspaceSessionCodec.SCALE, "1"),
            WorkspaceSessionCodec.OFFSET_X to preferences.string(WorkspaceSessionCodec.OFFSET_X, "0"),
            WorkspaceSessionCodec.OFFSET_Y to preferences.string(WorkspaceSessionCodec.OFFSET_Y, "0"),
            WorkspaceSessionCodec.SELECTION_IDS to preferences.string(WorkspaceSessionCodec.SELECTION_IDS, ""),
            WorkspaceSessionCodec.DOCUMENT_SCROLL to preferences.string(WorkspaceSessionCodec.DOCUMENT_SCROLL, "0"),
            WorkspaceSessionCodec.DOCUMENT_SCROLL_INDEX to preferences.string(WorkspaceSessionCodec.DOCUMENT_SCROLL_INDEX, "0"),
            WorkspaceSessionCodec.PAGE_LIST_INDEX to preferences.string(WorkspaceSessionCodec.PAGE_LIST_INDEX, "0"),
            WorkspaceSessionCodec.PAGE_LIST_OFFSET to preferences.string(WorkspaceSessionCodec.PAGE_LIST_OFFSET, "0")
        )
    )

    fun writeSession(session: WorkspaceSession) {
        session.encode().forEach { (key, value) -> preferences.putString(key, value) }
    }

    fun readLanguage(systemLanguage: String): AppLanguage =
        AppLanguage.fromCode(preferences.string("language", systemLanguage))

    fun writeLanguage(language: AppLanguage) { preferences.putString("language", language.code) }

    fun readCanvasTools(): CanvasToolPreferences = CanvasToolPreferences(
        penOnly = preferences.boolean("pen_only", false),
        eraserArea = preferences.boolean("eraser_area", false),
        brushKind = preferences.string("brush_kind", BrushSettings.KIND_BRUSH) ?: BrushSettings.KIND_BRUSH,
        highlighterColor = preferences.int("highlighter_color", 0xffe8c64b.toInt()),
        highlighterWidth = preferences.float("highlighter_width", BrushSettings.DEFAULT_HIGHLIGHTER_WIDTH).coerceIn(10f, 30f),
        inkColor = preferences.int("color", InkStyle().color),
        inkWidth = preferences.float("width", 3f).coerceIn(1f, 12f)
    )

    fun readSettings(legacyMotionEnabled: Boolean? = null): WorkspaceSettings =
        preferences.readWorkspaceSettings(legacyMotionEnabled)

    fun writeSettings(settings: WorkspaceSettings) { preferences.writeWorkspaceSettings(settings) }

    fun savePenOnly(value: Boolean) { preferences.putBoolean("pen_only", value) }
    fun saveEraserArea(value: Boolean) { preferences.putBoolean("eraser_area", value) }
    fun saveBrushKind(value: String) { preferences.putString("brush_kind", value) }
    fun saveHighlighterColor(value: Int) { preferences.putInt("highlighter_color", value) }
    fun saveHighlighterWidth(value: Float) { preferences.putFloat("highlighter_width", value) }
    fun saveInkColor(value: Int) { preferences.putInt("color", value) }
    fun saveInkWidth(value: Float) { preferences.putFloat("width", value) }
}
