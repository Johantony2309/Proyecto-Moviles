package com.example.proyectopanaderia.data.local.database

/** Single source of truth for the demo catalog so the callback and repository stay in sync. */
internal object CatalogSeed {
    data class SeedProduct(
        val name: String,
        val description: String,
        val priceCents: Long,
        val imageKey: String,
    )

    /** A unique name occupies its slot even if the user customized the row. */
    fun variantDescription(categoryName: String, product: SeedProduct): String? =
        product.description.takeIf { categories.getValue(categoryName).count { it.name == product.name } > 1 }

    /** Category name (in display order) to its products. */
    val categories: Map<String, List<SeedProduct>> = linkedMapOf(
        "Panes" to listOf(
            SeedProduct("Croissant", "Mantequilla · 1 unidad", 150, "croissant"),
            SeedProduct("Pan de campo", "Masa madre · 500 g", 325, "bread"),
            SeedProduct("Guagua", "Masa enriquecida al estilo brioche · 1 unidad", 200, "guagua"),
        ),
        "Pasteles" to listOf(
            SeedProduct("Torta de chocolate", "Mediana · 8 porciones", 800, "cake"),
            SeedProduct("Torta Andony", "Mediana · 15 porciones", 1000, "torta_andony"),
            SeedProduct("Torta de Vainilla", "Mediana · 12 porciones", 800, "torta_vainilla"),
            SeedProduct("Torta ChocoVainilla", "Mediana · 12 porciones", 900, "torta_chocovainilla"),
            SeedProduct("Torta Manabi de chocolate", "Mediana · 15 porciones", 1200, "torta_manabi_chocolate"),
            SeedProduct("Torta de Naranja", "Mediana · 20 porciones", 1700, "torta_naranja"),
            SeedProduct("Torta de Piña", "Mediana · 12 porciones", 900, "torta_pina"),
            SeedProduct("Torta ChocoBlanco", "Mediana · 15 porciones", 1200, "torta_chocoblanco"),
            SeedProduct("Torta ChocoNegro", "Mediana · 15 porciones", 1500, "torta_choconegro"),
            SeedProduct("Torta de Fresa", "Grande · 28 porciones", 2000, "torta_fresa"),
            SeedProduct("Torta de Zanahoria", "Grande · 28 porciones", 2000, "torta_zanahoria"),
            SeedProduct("Torta de Rompope", "Grande · 28 porciones", 2000, "torta_rompope"),
        ),
        "Dulces" to listOf(
            SeedProduct("Rollo de canela", "Canela y glaseado", 225, "roll"),
            SeedProduct("Caja de alfajores", "6 unidades", 150, "caja_alfajores"),
            SeedProduct("Bolitas de coco", "12 unidades", 100, "bolitas_coco"),
            SeedProduct("Galletitas Chavelitas", "24 unidades", 100, "galletitas_chavelitas"),
            SeedProduct("Funda Huevitos Moyo", "24 unidades", 100, "funda_huevitos_moyo"),
            SeedProduct("Funda de Rombitos", "12 unidades", 125, "funda_rombitos"),
            SeedProduct("Funda de Suspiros", "50 unidades", 200, "funda_suspiros"),
            SeedProduct("Funda Galletitas Almidon", "12 unidades", 100, "funda_galletitas_almidon"),
        ),
        "Postres" to listOf(
            SeedProduct("Copa Cremosa de chocolate", "1 unidad", 200, "copa_chocolate"),
            SeedProduct("Copa Cremosa de frutos rojos", "1 unidad", 200, "copa_frutos_rojos"),
            SeedProduct("Copa Cremosa de maracuya", "1 unidad", 200, "copa_maracuya"),
            SeedProduct("Copa Cremosa de oreo", "1 unidad", 200, "copa_oreo"),
            SeedProduct("Cuchariable de Chocolate", "1 unidad", 500, "cuchariable_chocolate"),
            SeedProduct("Cuchariable de vainilla", "1 unidad", 500, "cuchariable_vainilla"),
            SeedProduct("Porcion ChocoFlan", "1 unidad", 150, "porcion_chocoflan"),
            SeedProduct("Porcion Tres Leches", "1 unidad", 200, "porcion_tres_leches"),
            SeedProduct("Porcion Mojada de Chocolate", "1 unidad", 150, "porcion_mojada_chocolate"),
        ),
        "Bebidas" to listOf(
            SeedProduct("Colada Morada", "1 litro", 550, "colada_morada_litro"),
            SeedProduct("Colada Morada", "1/2 litro", 275, "colada_morada_medio"),
            SeedProduct("Rompope", "1 litro", 350, "rompope"),
        ),
    )
}
