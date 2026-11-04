package com.example.tabletworkspace

data class GalleryTemplate(val title: String, val resId: Int, val aspect: Float = 1f)
data class GalleryCategory(val key: String, val name: String, val templates: List<GalleryTemplate>)

object TemplateGalleryData {
    val categories = listOf(
        GalleryCategory("arrows", "Arrows", listOf(
            GalleryTemplate("Arrow 1", R.drawable.tpl_arrows_01, 1f),
            GalleryTemplate("Arrow 2", R.drawable.tpl_arrows_02, 1f),
            GalleryTemplate("Arrow 3", R.drawable.tpl_arrows_03, 1f),
            GalleryTemplate("Arrow 4", R.drawable.tpl_arrows_04, 1f),
            GalleryTemplate("Arrow 5", R.drawable.tpl_arrows_05, 1f),
            GalleryTemplate("Arrow 6", R.drawable.tpl_arrows_06, 1f),
            GalleryTemplate("Arrow 7", R.drawable.tpl_arrows_07, 1f),
            GalleryTemplate("Arrow 8", R.drawable.tpl_arrows_08, 1f),
            GalleryTemplate("Arrow 9", R.drawable.tpl_arrows_09, 1f),
            GalleryTemplate("Arrow 10", R.drawable.tpl_arrows_10, 1f),
            GalleryTemplate("Arrow 11", R.drawable.tpl_arrows_11, 1f),
            GalleryTemplate("Arrow 12", R.drawable.tpl_arrows_12, 1f),
            GalleryTemplate("Arrow 13", R.drawable.tpl_arrows_13, 1f),
            GalleryTemplate("Arrow 14", R.drawable.tpl_arrows_14, 1f),
            GalleryTemplate("Arrow 15", R.drawable.tpl_arrows_15, 1f),
            GalleryTemplate("Arrow 16", R.drawable.tpl_arrows_16, 1f),
            GalleryTemplate("Arrow 17", R.drawable.tpl_arrows_17, 1f),
            GalleryTemplate("Arrow 18", R.drawable.tpl_arrows_18, 1f)
        )),
        GalleryCategory("cheeky-piggies", "Cheeky Piggies", listOf(
            GalleryTemplate("Crybaby", R.drawable.tpl_cheeky_piggies_01, 1f),
            GalleryTemplate("Drool", R.drawable.tpl_cheeky_piggies_02, 1f),
            GalleryTemplate("Fuming", R.drawable.tpl_cheeky_piggies_03, 1f),
            GalleryTemplate("Hi~", R.drawable.tpl_cheeky_piggies_04, 1f),
            GalleryTemplate("Holding Tears", R.drawable.tpl_cheeky_piggies_05, 1f),
            GalleryTemplate("Love Blows", R.drawable.tpl_cheeky_piggies_06, 1f),
            GalleryTemplate("Me  Really", R.drawable.tpl_cheeky_piggies_07, 1f),
            GalleryTemplate("OK", R.drawable.tpl_cheeky_piggies_08, 1f),
            GalleryTemplate("Sassy Flick", R.drawable.tpl_cheeky_piggies_09, 1f),
            GalleryTemplate("Shockwave", R.drawable.tpl_cheeky_piggies_10, 1f),
            GalleryTemplate("Snooze Drool", R.drawable.tpl_cheeky_piggies_11, 1f),
            GalleryTemplate("Swag", R.drawable.tpl_cheeky_piggies_12, 1f),
            GalleryTemplate("Sweatdrop", R.drawable.tpl_cheeky_piggies_13, 1f),
            GalleryTemplate("Thumbs Up", R.drawable.tpl_cheeky_piggies_14, 1f),
            GalleryTemplate("What", R.drawable.tpl_cheeky_piggies_15, 1f)
        )),
        GalleryCategory("contorted-stickers", "Contorted Stickers", listOf(
            GalleryTemplate("AFFiNE", R.drawable.tpl_contorted_stickers_01, 1f),
            GalleryTemplate("AI", R.drawable.tpl_contorted_stickers_02, 1f),
            GalleryTemplate("Cat", R.drawable.tpl_contorted_stickers_03, 1f),
            GalleryTemplate("Closed", R.drawable.tpl_contorted_stickers_04, 1f),
            GalleryTemplate("Eyes", R.drawable.tpl_contorted_stickers_05, 1f),
            GalleryTemplate("Fire", R.drawable.tpl_contorted_stickers_06, 1f),
            GalleryTemplate("Info", R.drawable.tpl_contorted_stickers_07, 1f),
            GalleryTemplate("King", R.drawable.tpl_contorted_stickers_08, 1f),
            GalleryTemplate("Love Face", R.drawable.tpl_contorted_stickers_09, 1f),
            GalleryTemplate("Love", R.drawable.tpl_contorted_stickers_10, 1f),
            GalleryTemplate("Notice", R.drawable.tpl_contorted_stickers_11, 1f),
            GalleryTemplate("Pin", R.drawable.tpl_contorted_stickers_12, 1f),
            GalleryTemplate("Question", R.drawable.tpl_contorted_stickers_13, 1f),
            GalleryTemplate("Smile Face", R.drawable.tpl_contorted_stickers_14, 1f),
            GalleryTemplate("Stop", R.drawable.tpl_contorted_stickers_15, 1f)
        )),
        GalleryCategory("paper", "Paper", listOf(
            GalleryTemplate("+1", R.drawable.tpl_paper_01, 1f),
            GalleryTemplate("A lot of question", R.drawable.tpl_paper_02, 1f),
            GalleryTemplate("AFFiNE AI", R.drawable.tpl_paper_03, 1f),
            GalleryTemplate("Arrow", R.drawable.tpl_paper_04, 1f),
            GalleryTemplate("Atention", R.drawable.tpl_paper_05, 1f),
            GalleryTemplate("Blue Screen", R.drawable.tpl_paper_06, 1f),
            GalleryTemplate("Boom", R.drawable.tpl_paper_07, 1f),
            GalleryTemplate("Cool", R.drawable.tpl_paper_08, 1f),
            GalleryTemplate("Dino", R.drawable.tpl_paper_09, 1f),
            GalleryTemplate("Histogram", R.drawable.tpl_paper_10, 1f),
            GalleryTemplate("Local First", R.drawable.tpl_paper_11, 1f),
            GalleryTemplate("Medal", R.drawable.tpl_paper_12, 1f),
            GalleryTemplate("Notice", R.drawable.tpl_paper_13, 1f),
            GalleryTemplate("Pin", R.drawable.tpl_paper_14, 1f),
            GalleryTemplate("Star", R.drawable.tpl_paper_15, 1f)
        )),
        GalleryCategory("brainstorming", "Brainstorming", listOf(
            GalleryTemplate("Who What Where", R.drawable.tpl_brainstorming_01, 1.6875f),
            GalleryTemplate("Flowchart", R.drawable.tpl_brainstorming_02, 1.6875f),
            GalleryTemplate("Idea Clusters", R.drawable.tpl_brainstorming_03, 1.6875f),
            GalleryTemplate("SMART Goals", R.drawable.tpl_brainstorming_04, 1.6875f),
            GalleryTemplate("Wireframe", R.drawable.tpl_brainstorming_05, 1.6875f)
        )),
    )

    fun category(key: String): GalleryCategory = categories.firstOrNull { it.key == key } ?: categories.first()
}
