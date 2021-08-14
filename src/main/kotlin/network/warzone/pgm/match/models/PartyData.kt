package network.warzone.pgm.match.models

data class PartyData(
    val name: String,
    val alias: String,
    val colour: String,
    val min: Int,
    val max: Int
)