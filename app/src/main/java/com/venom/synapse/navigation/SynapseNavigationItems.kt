package com.venom.synapse.navigation

import com.venom.synapse.R
import com.venom.ui.navigation.NavigationItem

enum class SynapseNavigationItems(
    override val route: String,
    override val icon: Int,
    override val iconActive: Int,
    override val titleRes: Int,
    override val showInBottomBar: Boolean = true,
) : NavigationItem {

    DASHBOARD(
        route    = SynapseScreen.Dashboard.route,
        icon     = R.drawable.ic_home,
        iconActive = R.drawable.ic_home_fill,
        titleRes = R.string.synapse_nav_home,
    ),
    LIBRARY(
        route    = SynapseScreen.Library.route,
        icon     = R.drawable.ic_book_open,
        iconActive = R.drawable.ic_book_open_fill,
        titleRes = R.string.synapse_nav_library,
    ),
    STATS(
        route    = SynapseScreen.Stats.route,
        icon     = R.drawable.ic_bar_chart,
        iconActive = R.drawable.ic_bar_chart_fill,
        titleRes = R.string.synapse_nav_stats,
    );

    companion object {
        val visibleItems: List<SynapseNavigationItems> =
            entries.filter { it.showInBottomBar }
    }
}
