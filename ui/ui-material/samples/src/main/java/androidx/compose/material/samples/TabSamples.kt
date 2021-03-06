/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.material.samples

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.transitionDefinition
import androidx.annotation.Sampled
import androidx.compose.Composable
import androidx.compose.emptyContent
import androidx.compose.getValue
import androidx.compose.remember
import androidx.compose.setValue
import androidx.compose.state
import androidx.compose.animation.ColorPropKey
import androidx.compose.animation.DpPropKey
import androidx.compose.animation.transition
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.compose.foundation.Border
import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.drawBorder
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.unit.dp

@Sampled
@Composable
fun TextTabs() {
    var state by state { 0 }
    val titles = listOf("TAB 1", "TAB 2", "TAB 3 WITH LOTS OF TEXT")
    Column {
        TabRow(items = titles, selectedIndex = state) { index, text ->
            Tab(text = { Text(text) }, selected = state == index, onSelected = { state = index })
        }
        Text(
            modifier = Modifier.gravity(Alignment.CenterHorizontally),
            text = "Text tab ${state + 1} selected",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun IconTabs() {
    var state by state { 0 }
    val icons = listOf(Icons.Filled.Favorite, Icons.Filled.Favorite, Icons.Filled.Favorite)
    Column {
        TabRow(items = icons, selectedIndex = state) { index, icon ->
            Tab(icon = { Icon(icon) }, selected = state == index, onSelected = { state = index })
        }
        Text(
            modifier = Modifier.gravity(Alignment.CenterHorizontally),
            text = "Icon tab ${state + 1} selected",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun TextAndIconTabs() {
    var state by state { 0 }
    val titlesAndIcons = listOf(
        "TAB 1" to Icons.Filled.Favorite,
        "TAB 2" to Icons.Filled.Favorite,
        "TAB 3 WITH LOTS OF TEXT" to Icons.Filled.Favorite
    )
    Column {
        TabRow(items = titlesAndIcons, selectedIndex = state) { index, (title, icon) ->
            Tab(
                text = { Text(title) },
                icon = { Icon(icon) },
                selected = state == index,
                onSelected = { state = index }
            )
        }
        Text(
            modifier = Modifier.gravity(Alignment.CenterHorizontally),
            text = "Text and icon tab ${state + 1} selected",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun ScrollingTextTabs() {
    var state by state { 0 }
    val titles = listOf(
        "TAB 1",
        "TAB 2",
        "TAB 3 WITH LOTS OF TEXT",
        "TAB 4",
        "TAB 5",
        "TAB 6 WITH LOTS OF TEXT",
        "TAB 7",
        "TAB 8",
        "TAB 9 WITH LOTS OF TEXT",
        "TAB 10"
    )
    Column {
        TabRow(items = titles, selectedIndex = state, scrollable = true) { index, text ->
            Tab(text = { Text(text) }, selected = state == index, onSelected = { state = index })
        }
        Text(
            modifier = Modifier.gravity(Alignment.CenterHorizontally),
            text = "Scrolling text tab ${state + 1} selected",
            style = MaterialTheme.typography.body1
        )
    }
}

@Sampled
@Composable
fun FancyTabs() {
    var state by state { 0 }
    val titles = listOf("TAB 1", "TAB 2", "TAB 3")
    Column {
        TabRow(items = titles, selectedIndex = state) { index, title ->
            FancyTab(title = title, onClick = { state = index }, selected = (index == state))
        }
        Text(
            modifier = Modifier.gravity(Alignment.CenterHorizontally),
            text = "Fancy tab ${state + 1} selected",
            style = MaterialTheme.typography.body1
        )
    }
}

@Sampled
@Composable
fun FancyIndicatorTabs() {
    var state by state { 0 }
    val titles = listOf("TAB 1", "TAB 2", "TAB 3")

    // Reuse the default transition, and provide our custom indicator as its child
    val indicatorContainer = @Composable { tabPositions: List<TabRow.TabPosition> ->
        TabRow.IndicatorContainer(tabPositions = tabPositions, selectedIndex = state) {
            FancyIndicator(Color.White)
        }
    }

    Column {
        TabRow(
            items = titles,
            selectedIndex = state,
            indicatorContainer = indicatorContainer
        ) { index, text ->
            Tab(text = { Text(text) }, selected = state == index, onSelected = { state = index })
        }
        Text(
            modifier = Modifier.gravity(Alignment.CenterHorizontally),
            text = "Fancy indicator tab ${state + 1} selected",
            style = MaterialTheme.typography.body1
        )
    }
}

@Sampled
@Composable
fun FancyIndicatorContainerTabs() {
    var state by state { 0 }
    val titles = listOf("TAB 1", "TAB 2", "TAB 3")

    val indicatorContainer = @Composable { tabPositions: List<TabRow.TabPosition> ->
        FancyIndicatorContainer(tabPositions = tabPositions, selectedIndex = state)
    }

    Column {
        TabRow(
            items = titles,
            selectedIndex = state,
            indicatorContainer = indicatorContainer
        ) { index, text ->
            Tab(text = { Text(text) }, selected = state == index, onSelected = { state = index })
        }
        Text(
            modifier = Modifier.gravity(Alignment.CenterHorizontally),
            text = "Fancy transition tab ${state + 1} selected",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun ScrollingFancyIndicatorContainerTabs() {
    var state by state { 0 }
    val titles = listOf(
        "TAB 1",
        "TAB 2",
        "TAB 3 WITH LOTS OF TEXT",
        "TAB 4",
        "TAB 5",
        "TAB 6 WITH LOTS OF TEXT",
        "TAB 7",
        "TAB 8",
        "TAB 9 WITH LOTS OF TEXT",
        "TAB 10"
    )
    val indicatorContainer = @Composable { tabPositions: List<TabRow.TabPosition> ->
        FancyIndicatorContainer(tabPositions = tabPositions, selectedIndex = state)
    }

    Column {
        TabRow(
            items = titles,
            selectedIndex = state,
            indicatorContainer = indicatorContainer,
            scrollable = true
        ) { index, text ->
            Tab(text = { Text(text) }, selected = state == index, onSelected = { state = index })
        }
        Text(
            modifier = Modifier.gravity(Alignment.CenterHorizontally),
            text = "Scrolling fancy transition tab ${state + 1} selected",
            style = MaterialTheme.typography.body1
        )
    }
}

@Sampled
@Composable
fun FancyTab(title: String, onClick: () -> Unit, selected: Boolean) {
    Tab(selected, onClick) {
        Column(
            Modifier.padding(10.dp).preferredHeight(50.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier.preferredSize(10.dp)
                    .gravity(Alignment.CenterHorizontally)
                    .background(color = if (selected) Color.Red else Color.White),
                children = emptyContent()
            )
            Text(
                text = title,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.gravity(Alignment.CenterHorizontally)
            )
        }
    }
}

@Sampled
@Composable
fun FancyIndicator(color: Color) {
    // Draws a rounded rectangular with border around the Tab, with a 5.dp padding from the edges
    // Color is passed in as a parameter [color]
    Stack(
        Modifier.padding(5.dp)
            .fillMaxSize()
            .drawBorder(Border(2.dp, color), RoundedCornerShape(5.dp))
    ) {}
}

@Sampled
@Composable
fun FancyIndicatorContainer(tabPositions: List<TabRow.TabPosition>, selectedIndex: Int) {
    val indicatorStart = remember { DpPropKey() }
    val indicatorEnd = remember { DpPropKey() }
    val indicatorColor = remember { ColorPropKey() }

    val colors = listOf(Color.Yellow, Color.Red, Color.Green)
    val transitionDefinition =
        remember(tabPositions) {
            transitionDefinition<Int> {
                tabPositions.forEachIndexed { index, position ->
                    state(index) {
                        this[indicatorStart] = position.left
                        this[indicatorEnd] = position.right
                        this[indicatorColor] = colors[index % colors.size]
                    }
                }
                repeat(tabPositions.size) { from ->
                    repeat(tabPositions.size) { to ->
                        if (from != to) {
                            transition(fromState = from, toState = to) {
                                // Handle directionality here, if we are moving to the right, we
                                // want the right side of the indicator to move faster, if we are
                                // moving to the left, we want the left side to move faster.
                                val startStiffness = if (from < to) 50f else 1000f
                                val endStiffness = if (from < to) 1000f else 50f
                                indicatorStart using spring(
                                    dampingRatio = 1f,
                                    stiffness = startStiffness
                                )
                                indicatorEnd using spring(
                                    dampingRatio = 1f,
                                    stiffness = endStiffness
                                )
                            }
                        }
                    }
                }
            }
        }

    // Fill up the entire TabRow with this container, and place children at the left so we can use
    // Padding to set the 'offset'
    Box(Modifier.fillMaxSize(), gravity = ContentGravity.BottomStart) {
        val state = transition(transitionDefinition, selectedIndex)
        val offset = state[indicatorStart]
        val width = state[indicatorEnd] - state[indicatorStart]
        Box(
            Modifier.offset(x = offset, y = 0.dp).preferredWidth(width),
            gravity = ContentGravity.Center
        ) {
            // Pass the current color to the indicator
            FancyIndicator(state[indicatorColor])
        }
    }
}
