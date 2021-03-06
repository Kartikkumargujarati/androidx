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

package androidx.ui.core

import androidx.compose.Applier
import androidx.compose.Composable
import androidx.compose.ExperimentalComposeApi
import androidx.compose.Stable
import androidx.compose.currentComposer
import androidx.compose.emit
import androidx.compose.remember
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import kotlin.math.max

/**
 * [Layout] is the main core component for layout. It can be used to measure and position
 * zero or more children.
 *
 * Intrinsic measurement blocks define the intrinsic sizes of the current layout. These
 * can be queried by the parent in order to understand, in specific cases, what constraints
 * should the layout be measured with:
 * - [minIntrinsicWidthMeasureBlock] defines the minimum width this layout can take, given
 *   a specific height, such that the content of the layout will be painted correctly
 * - [minIntrinsicHeightMeasureBlock] defines the minimum height this layout can take, given
 *   a specific width, such that the content of the layout will be painted correctly
 * - [maxIntrinsicWidthMeasureBlock] defines the minimum width such that increasing it further
 *   will not decrease the minimum intrinsic height
 * - [maxIntrinsicHeightMeasureBlock] defines the minimum height such that increasing it further
 *   will not decrease the minimum intrinsic width
 *
 * For a composable able to define its content according to the incoming constraints,
 * see [WithConstraints].
 *
 * Example usage:
 * @sample androidx.ui.core.samples.LayoutWithProvidedIntrinsicsUsage
 *
 * @param children The children composable to be laid out.
 * @param modifier Modifiers to be applied to the layout.
 * @param minIntrinsicWidthMeasureBlock The minimum intrinsic width of the layout.
 * @param minIntrinsicHeightMeasureBlock The minimum intrinsic height of the layout.
 * @param maxIntrinsicWidthMeasureBlock The maximum intrinsic width of the layout.
 * @param maxIntrinsicHeightMeasureBlock The maximum intrinsic height of the layout.
 * @param measureBlock The block defining the measurement and positioning of the layout.
 *
 * @see Layout
 * @see WithConstraints
 */
@Composable
@OptIn(ExperimentalLayoutNodeApi::class)
fun Layout(
    children: @Composable () -> Unit,
    minIntrinsicWidthMeasureBlock: IntrinsicMeasureBlock2,
    minIntrinsicHeightMeasureBlock: IntrinsicMeasureBlock2,
    maxIntrinsicWidthMeasureBlock: IntrinsicMeasureBlock2,
    maxIntrinsicHeightMeasureBlock: IntrinsicMeasureBlock2,
    modifier: Modifier = Modifier,
    measureBlock: MeasureBlock2
) {
    val measureBlocks = measureBlocksOf(
        minIntrinsicWidthMeasureBlock,
        minIntrinsicHeightMeasureBlock,
        maxIntrinsicWidthMeasureBlock,
        maxIntrinsicHeightMeasureBlock,
        measureBlock
    )
    Layout(children, measureBlocks, modifier)
}

/**
 * Creates an instance of [LayoutNode.MeasureBlocks] to pass to [Layout] given
 * intrinsic measures and a measure block.
 *
 * @sample androidx.ui.core.samples.LayoutWithMeasureBlocksWithIntrinsicUsage
 *
 * Intrinsic measurement blocks define the intrinsic sizes of the current layout. These
 * can be queried by the parent in order to understand, in specific cases, what constraints
 * should the layout be measured with:
 * - [minIntrinsicWidthMeasureBlock] defines the minimum width this layout can take, given
 *   a specific height, such that the content of the layout will be painted correctly
 * - [minIntrinsicHeightMeasureBlock] defines the minimum height this layout can take, given
 *   a specific width, such that the content of the layout will be painted correctly
 * - [maxIntrinsicWidthMeasureBlock] defines the minimum width such that increasing it further
 *   will not decrease the minimum intrinsic height
 * - [maxIntrinsicHeightMeasureBlock] defines the minimum height such that increasing it further
 *   will not decrease the minimum intrinsic width
 *
 * For a composable able to define its content according to the incoming constraints,
 * see [WithConstraints].
 *
 * @param minIntrinsicWidthMeasureBlock The minimum intrinsic width of the layout.
 * @param minIntrinsicHeightMeasureBlock The minimum intrinsic height of the layout.
 * @param maxIntrinsicWidthMeasureBlock The maximum intrinsic width of the layout.
 * @param maxIntrinsicHeightMeasureBlock The maximum intrinsic height of the layout.
 * @param measureBlock The block defining the measurement and positioning of the layout.
 *
 * @see Layout
 * @see WithConstraints
 */
@ExperimentalLayoutNodeApi
fun measureBlocksOf(
    minIntrinsicWidthMeasureBlock: IntrinsicMeasureBlock2,
    minIntrinsicHeightMeasureBlock: IntrinsicMeasureBlock2,
    maxIntrinsicWidthMeasureBlock: IntrinsicMeasureBlock2,
    maxIntrinsicHeightMeasureBlock: IntrinsicMeasureBlock2,
    measureBlock: MeasureBlock2
): LayoutNode.MeasureBlocks {
    return object : LayoutNode.MeasureBlocks {
        override fun measure(
            measureScope: MeasureScope,
            measurables: List<Measurable>,
            constraints: Constraints,
            layoutDirection: LayoutDirection
        ) = measureScope.measureBlock(measurables, constraints)
        override fun minIntrinsicWidth(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            h: Int,
            layoutDirection: LayoutDirection
        ): Int {
            val receiver = IntrinsicsMeasureScope(intrinsicMeasureScope, layoutDirection)
            return receiver.minIntrinsicWidthMeasureBlock(measurables, h)
        }
        override fun minIntrinsicHeight(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            w: Int,
            layoutDirection: LayoutDirection
        ): Int {
            val receiver = IntrinsicsMeasureScope(intrinsicMeasureScope, layoutDirection)
            return receiver.minIntrinsicHeightMeasureBlock(measurables, w)
        }
        override fun maxIntrinsicWidth(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            h: Int,
            layoutDirection: LayoutDirection
        ): Int {
            val receiver = IntrinsicsMeasureScope(intrinsicMeasureScope, layoutDirection)
            return receiver.maxIntrinsicWidthMeasureBlock(measurables, h)
        }
        override fun maxIntrinsicHeight(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            w: Int,
            layoutDirection: LayoutDirection
        ): Int {
            val receiver = IntrinsicsMeasureScope(intrinsicMeasureScope, layoutDirection)
            return receiver.maxIntrinsicHeightMeasureBlock(measurables, w)
        }
    }
}

@Deprecated(
    "Use Layout() function with 'MeasureScope.(List<Measurable>, Constraints) -> MeasureScope" +
            ".MeasureResult' measure block and 'IntrinsicMeasureScope.(List<IntrinsicMeasurable>," +
            " Int) -> Int' intrinsic measure blocks instead.",
    ReplaceWith("Layout(children, {measurable, height -> minIntrinsicWidthMeasureBlock}, " +
            "{measurable, width -> minIntrinsicHeightMeasureBlock}, " +
            "{measurable, height -> maxIntrinsicWidthMeasureBlock}, " +
            "{measurable, width -> maxIntrinsicHeightMeasureBlock}," +
            " modifier, " +
            "{measurables, constraints -> measureBlock})"
    )
)
@Suppress("DEPRECATION")
@Composable
@OptIn(ExperimentalLayoutNodeApi::class)
/*inline*/ fun Layout(
    /*crossinline*/
    children: @Composable () -> Unit,
    /*crossinline*/
    minIntrinsicWidthMeasureBlock: IntrinsicMeasureBlock,
    /*crossinline*/
    minIntrinsicHeightMeasureBlock: IntrinsicMeasureBlock,
    /*crossinline*/
    maxIntrinsicWidthMeasureBlock: IntrinsicMeasureBlock,
    /*crossinline*/
    maxIntrinsicHeightMeasureBlock: IntrinsicMeasureBlock,
    modifier: Modifier = Modifier,
    /*crossinline*/
    measureBlock: MeasureBlock
) {
    val measureBlocks = object : LayoutNode.MeasureBlocks {
        override fun measure(
            measureScope: MeasureScope,
            measurables: List<Measurable>,
            constraints: Constraints,
            layoutDirection: LayoutDirection
        ) = measureScope.measureBlock(measurables, constraints, layoutDirection)
        override fun minIntrinsicWidth(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            h: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.minIntrinsicWidthMeasureBlock(measurables, h, layoutDirection)
        override fun minIntrinsicHeight(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            w: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.minIntrinsicHeightMeasureBlock(measurables, w, layoutDirection)
        override fun maxIntrinsicWidth(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            h: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.maxIntrinsicWidthMeasureBlock(measurables, h, layoutDirection)
        override fun maxIntrinsicHeight(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            w: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.maxIntrinsicHeightMeasureBlock(measurables, w, layoutDirection)
    }
    Layout(children, measureBlocks, modifier)
}

/**
 * [Layout] is the main core component for layout. It can be used to measure and position
 * zero or more children.
 *
 * The intrinsic measurements of this layout will be calculated by running the measureBlock,
 * while swapping measure calls with appropriate intrinsic measurements. Note that these
 * provided implementations will not be accurate in all cases - when this happens, the other
 * overload of [Layout] should be used to provide correct measurements.
 *
 * For a composable able to define its content according to the incoming constraints,
 * see [WithConstraints].
 *
 * Example usage:
 * @sample androidx.ui.core.samples.LayoutUsage
 *
 * @param children The children composable to be laid out.
 * @param modifier Modifiers to be applied to the layout.
 * @param measureBlock The block defining the measurement and positioning of the layout.
 *
 * @see Layout
 * @see WithConstraints
 */
@Composable
@OptIn(ExperimentalLayoutNodeApi::class)
/*inline*/ fun Layout(
    /*crossinline*/
    children: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    /*noinline*/
    measureBlock: MeasureBlock2
) {
    val measureBlocks = remember(measureBlock) { MeasuringIntrinsicsMeasureBlocks(measureBlock) }
    Layout(children, measureBlocks, modifier)
}

@Deprecated(
    """Use Layout() function with
        'MeasureScope.(List<Measurable>, Constraints) -> MeasureScope.MeasureResult'
        measure block instead.""",
    ReplaceWith("Layout(children, modifier, {measurables, constraints -> measureBlock})")
)
@Suppress("DEPRECATION")
@Composable
@OptIn(ExperimentalLayoutNodeApi::class)
/*inline*/ fun Layout(
    /*crossinline*/
    children: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    /*noinline*/
    measureBlock: MeasureBlock
) {

    val measureBlocks = remember(measureBlock) { MeasuringIntrinsicsMeasureBlocks(measureBlock) }
    Layout(children, measureBlocks, modifier)
}

/**
 * [Layout] is the main core component for layout. It can be used to measure and position
 * zero or more children.
 *
 * The intrinsic measurements of this layout will be calculated by using the [measureBlocks]
 * instance.
 *
 * For a composable able to define its content according to the incoming constraints,
 * see [WithConstraints].
 *
 * Example usage:
 * @sample androidx.ui.core.samples.LayoutWithMeasureBlocksWithIntrinsicUsage
 *
 * @param children The children composable to be laid out.
 * @param modifier Modifiers to be applied to the layout.
 * @param measureBlocks An [LayoutNode.MeasureBlocks] instance defining the measurement and
 * positioning of the layout.
 *
 * @see Layout
 * @see measureBlocksOf
 * @see WithConstraints
 */

@ExperimentalLayoutNodeApi
@Composable inline fun Layout(
    children: @Composable () -> Unit,
    measureBlocks: LayoutNode.MeasureBlocks,
    modifier: Modifier = Modifier
) {
    val materialized = currentComposer.materialize(modifier)

    @OptIn(ExperimentalComposeApi::class)
    emit<LayoutNode, Applier<Any>>(
        ctor = LayoutEmitHelper.constructor,
        update = {
            set(materialized, LayoutEmitHelper.setModifier)
            set(measureBlocks, LayoutEmitHelper.setMeasureBlocks)
        },
        children = children
    )
}

@Composable
@Deprecated("This composable is temporary to enable quicker prototyping in ConstraintLayout. " +
        "It should not be used in app code directly.")
@OptIn(ExperimentalLayoutNodeApi::class)
fun MultiMeasureLayout(
    modifier: Modifier = Modifier,
    children: @Composable () -> Unit,
    measureBlock: MeasureBlock2
) {
    val measureBlocks = remember(measureBlock) { MeasuringIntrinsicsMeasureBlocks(measureBlock) }
    val materialized = currentComposer.materialize(modifier)
    @OptIn(ExperimentalComposeApi::class)
    emit<LayoutNode, Applier<Any>>(
        ctor = LayoutEmitHelper.constructor,
        update = {
            set(materialized, LayoutEmitHelper.setModifier)
            set(measureBlocks, LayoutEmitHelper.setMeasureBlocks)
            @Suppress("DEPRECATION")
            set(Unit) { this.canMultiMeasure = true }
        },
        children = children
    )
}

/**
 * Used to return a fixed sized item for intrinsics measurements in [Layout]
 */
private class DummyPlaceable(width: Int, height: Int) : Placeable() {
    init {
        measuredSize = IntSize(width, height)
    }
    override fun get(line: AlignmentLine): Int = AlignmentLine.Unspecified
    override fun place(position: IntOffset) { }
}

/**
 * Identifies an [IntrinsicMeasurable] as a min or max intrinsic measurement.
 */
@PublishedApi
internal enum class IntrinsicMinMax {
    Min, Max
}

/**
 * Identifies an [IntrinsicMeasurable] as a width or height intrinsic measurement.
 */
@PublishedApi
internal enum class IntrinsicWidthHeight {
    Width, Height
}

/**
 * A wrapper around a [Measurable] for intrinsic measurments in [Layout]. Consumers of
 * [Layout] don't identify intrinsic methods, but we can give a reasonable implementation
 * by using their [measure], substituting the intrinsics gathering method
 * for the [Measurable.measure] call.
 */
@PublishedApi
internal class DefaultIntrinsicMeasurable(
    val measurable: IntrinsicMeasurable,
    val minMax: IntrinsicMinMax,
    val widthHeight: IntrinsicWidthHeight
) : Measurable {
    override val parentData: Any?
        get() = measurable.parentData

    override fun measure(constraints: Constraints, layoutDirection: LayoutDirection): Placeable {
        if (widthHeight == IntrinsicWidthHeight.Width) {
            val width = if (minMax == IntrinsicMinMax.Max) {
                measurable.maxIntrinsicWidth(constraints.maxHeight, layoutDirection)
            } else {
                measurable.minIntrinsicWidth(constraints.maxHeight, layoutDirection)
            }
            return DummyPlaceable(width, constraints.maxHeight)
        }
        val height = if (minMax == IntrinsicMinMax.Max) {
            measurable.maxIntrinsicHeight(constraints.maxWidth, layoutDirection)
        } else {
            measurable.minIntrinsicHeight(constraints.maxWidth, layoutDirection)
        }
        return DummyPlaceable(constraints.maxWidth, height)
    }

    override fun minIntrinsicWidth(height: Int, layoutDirection: LayoutDirection): Int {
        return measurable.minIntrinsicWidth(height, layoutDirection)
    }

    override fun maxIntrinsicWidth(height: Int, layoutDirection: LayoutDirection): Int {
        return measurable.maxIntrinsicWidth(height, layoutDirection)
    }

    override fun minIntrinsicHeight(width: Int, layoutDirection: LayoutDirection): Int {
        return measurable.minIntrinsicHeight(width, layoutDirection)
    }

    override fun maxIntrinsicHeight(width: Int, layoutDirection: LayoutDirection): Int {
        return measurable.maxIntrinsicHeight(width, layoutDirection)
    }
}

/**
 * Receiver scope for [Layout]'s layout lambda when used in an intrinsics call.
 */
@PublishedApi
@OptIn(ExperimentalLayoutNodeApi::class)
internal class IntrinsicsMeasureScope(
    density: Density,
    override val layoutDirection: LayoutDirection
) : MeasureScope(), Density by density
/**
 * Default [LayoutNode.MeasureBlocks] object implementation, providing intrinsic measurements
 * that use the measure block replacing the measure calls with intrinsic measurement calls.
 */
@OptIn(ExperimentalLayoutNodeApi::class)
fun MeasuringIntrinsicsMeasureBlocks(measureBlock: MeasureBlock2) =
    object : LayoutNode.MeasureBlocks {
        override fun measure(
            measureScope: MeasureScope,
            measurables: List<Measurable>,
            constraints: Constraints,
            layoutDirection: LayoutDirection
        ) = measureScope.measureBlock(measurables, constraints)
        override fun minIntrinsicWidth(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            h: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.MeasuringMinIntrinsicWidth(
            measureBlock,
            measurables,
            h,
            layoutDirection
        )
        override fun minIntrinsicHeight(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            w: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.MeasuringMinIntrinsicHeight(
            measureBlock,
            measurables,
            w,
            layoutDirection
        )
        override fun maxIntrinsicWidth(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            h: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.MeasuringMaxIntrinsicWidth(
            measureBlock,
            measurables,
            h,
            layoutDirection
        )
        override fun maxIntrinsicHeight(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            w: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.MeasuringMaxIntrinsicHeight(
            measureBlock,
            measurables,
            w,
            layoutDirection
        )

        override fun toString(): String {
            // this calls simpleIdentityToString on measureBlock because it is typically a lambda,
            // which has a useless toString that doesn't hint at the source location
            return simpleIdentityToString(
                this,
                "MeasuringIntrinsicsMeasureBlocks"
            ) + "{ measureBlock=${simpleIdentityToString(measureBlock, null)} }"
        }
    }

@OptIn(ExperimentalLayoutNodeApi::class)
@Deprecated("Use MeasuringIntrinsicsMeasureBlocks with MeasureBlock2 instead")
@Suppress("DEPRECATION")
fun MeasuringIntrinsicsMeasureBlocks(measureBlock: MeasureBlock) =
    object : LayoutNode.MeasureBlocks {
        override fun measure(
            measureScope: MeasureScope,
            measurables: List<Measurable>,
            constraints: Constraints,
            layoutDirection: LayoutDirection
        ) = measureScope.measureBlock(measurables, constraints, layoutDirection)
        override fun minIntrinsicWidth(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            h: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.MeasuringMinIntrinsicWidth(
            measureBlock,
            measurables,
            h,
            layoutDirection
        )
        override fun minIntrinsicHeight(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            w: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.MeasuringMinIntrinsicHeight(
            measureBlock,
            measurables,
            w,
            layoutDirection
        )
        override fun maxIntrinsicWidth(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            h: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.MeasuringMaxIntrinsicWidth(
            measureBlock,
            measurables,
            h,
            layoutDirection
        )
        override fun maxIntrinsicHeight(
            intrinsicMeasureScope: IntrinsicMeasureScope,
            measurables: List<IntrinsicMeasurable>,
            w: Int,
            layoutDirection: LayoutDirection
        ) = intrinsicMeasureScope.MeasuringMaxIntrinsicHeight(
            measureBlock,
            measurables,
            w,
            layoutDirection
        )

        override fun toString(): String {
            // this calls simpleIdentityToString on measureBlock because it is typically a lambda,
            // which has a useless toString that doesn't hint at the source location
            return simpleIdentityToString(
                this,
                "MeasuringIntrinsicsMeasureBlocks"
            ) + "{ measureBlock=${simpleIdentityToString(measureBlock, null)} }"
        }
    }

/**
 * Default implementation for the min intrinsic width of a layout. This works by running the
 * measure block with measure calls replaced with intrinsic measurement calls.
 */
private inline fun Density.MeasuringMinIntrinsicWidth(
    measureBlock: MeasureBlock2 /*TODO: crossinline*/,
    measurables: List<IntrinsicMeasurable>,
    h: Int,
    layoutDirection: LayoutDirection
): Int {
    val mapped = measurables.map {
        DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Min, IntrinsicWidthHeight.Width)
    }
    val constraints = Constraints(maxHeight = h)
    val layoutReceiver = IntrinsicsMeasureScope(this, layoutDirection)
    val layoutResult = layoutReceiver.measureBlock(mapped, constraints)
    return layoutResult.width
}

@Suppress("DEPRECATION")
private inline fun Density.MeasuringMinIntrinsicWidth(
    measureBlock: MeasureBlock /*TODO: crossinline*/,
    measurables: List<IntrinsicMeasurable>,
    h: Int,
    layoutDirection: LayoutDirection
): Int {
    val mapped = measurables.map {
        DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Min, IntrinsicWidthHeight.Width)
    }
    val constraints = Constraints(maxHeight = h)
    val layoutReceiver = IntrinsicsMeasureScope(this, layoutDirection)
    val layoutResult = layoutReceiver.measureBlock(mapped, constraints, layoutDirection)
    return layoutResult.width
}

/**
 * Default implementation for the min intrinsic width of a layout. This works by running the
 * measure block with measure calls replaced with intrinsic measurement calls.
 */
private inline fun Density.MeasuringMinIntrinsicHeight(
    measureBlock: MeasureBlock2 /*TODO: crossinline*/,
    measurables: List<IntrinsicMeasurable>,
    w: Int,
    layoutDirection: LayoutDirection
): Int {
    val mapped = measurables.map {
        DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Min, IntrinsicWidthHeight.Height)
    }
    val constraints = Constraints(maxWidth = w)
    val layoutReceiver = IntrinsicsMeasureScope(this, layoutDirection)
    val layoutResult = layoutReceiver.measureBlock(mapped, constraints)
    return layoutResult.height
}

@Suppress("DEPRECATION")
private inline fun Density.MeasuringMinIntrinsicHeight(
    measureBlock: MeasureBlock /*TODO: crossinline*/,
    measurables: List<IntrinsicMeasurable>,
    w: Int,
    layoutDirection: LayoutDirection
): Int {
    val mapped = measurables.map {
        DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Min, IntrinsicWidthHeight.Height)
    }
    val constraints = Constraints(maxWidth = w)
    val layoutReceiver = IntrinsicsMeasureScope(this, layoutDirection)
    val layoutResult = layoutReceiver.measureBlock(mapped, constraints, layoutDirection)
    return layoutResult.height
}

/**
 * Default implementation for the max intrinsic width of a layout. This works by running the
 * measure block with measure calls replaced with intrinsic measurement calls.
 */
private inline fun Density.MeasuringMaxIntrinsicWidth(
    measureBlock: MeasureBlock2 /*TODO: crossinline*/,
    measurables: List<IntrinsicMeasurable>,
    h: Int,
    layoutDirection: LayoutDirection
): Int {
    val mapped = measurables.map {
        DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Max, IntrinsicWidthHeight.Width)
    }
    val constraints = Constraints(maxHeight = h)
    val layoutReceiver = IntrinsicsMeasureScope(this, layoutDirection)
    val layoutResult = layoutReceiver.measureBlock(mapped, constraints)
    return layoutResult.width
}

@Suppress("DEPRECATION")
private inline fun Density.MeasuringMaxIntrinsicWidth(
    measureBlock: MeasureBlock /*TODO: crossinline*/,
    measurables: List<IntrinsicMeasurable>,
    h: Int,
    layoutDirection: LayoutDirection
): Int {
    val mapped = measurables.map {
        DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Max, IntrinsicWidthHeight.Width)
    }
    val constraints = Constraints(maxHeight = h)
    val layoutReceiver = IntrinsicsMeasureScope(this, layoutDirection)
    val layoutResult = layoutReceiver.measureBlock(mapped, constraints, layoutDirection)
    return layoutResult.width
}

/**
 * Default implementation for the max intrinsic height of a layout. This works by running the
 * measure block with measure calls replaced with intrinsic measurement calls.
 */
private inline fun Density.MeasuringMaxIntrinsicHeight(
    measureBlock: MeasureBlock2 /*TODO: crossinline*/,
    measurables: List<IntrinsicMeasurable>,
    w: Int,
    layoutDirection: LayoutDirection
): Int {
    val mapped = measurables.map {
        DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Max, IntrinsicWidthHeight.Height)
    }
    val constraints = Constraints(maxWidth = w)
    val layoutReceiver = IntrinsicsMeasureScope(this, layoutDirection)
    val layoutResult = layoutReceiver.measureBlock(mapped, constraints)
    return layoutResult.height
}

@Suppress("DEPRECATION")
private inline fun Density.MeasuringMaxIntrinsicHeight(
    measureBlock: MeasureBlock /*TODO: crossinline*/,
    measurables: List<IntrinsicMeasurable>,
    w: Int,
    layoutDirection: LayoutDirection
): Int {
    val mapped = measurables.map {
        DefaultIntrinsicMeasurable(it, IntrinsicMinMax.Max, IntrinsicWidthHeight.Height)
    }
    val constraints = Constraints(maxWidth = w)
    val layoutReceiver = IntrinsicsMeasureScope(this, layoutDirection)
    val layoutResult = layoutReceiver.measureBlock(mapped, constraints, layoutDirection)
    return layoutResult.height
}

/**
 * A composable that defines its own content according to the available space, based on the incoming
 * constraints or the current [LayoutDirection]. Example usage:
 * @sample androidx.ui.core.samples.WithConstraintsSample
 *
 * The composable will compose the given children, and will position the resulting layout composables
 * in a parent [Layout]. This layout will be as small as possible such that it can fit its
 * children. If the composition yields multiple layout children, these will be all placed at the
 * top left of the WithConstraints, so consider wrapping them in an additional common
 * parent if different positioning is preferred.
 *
 * @param modifier Modifier to be applied to the introduced layout.
 */
@OptIn(ExperimentalSubcomposeLayoutApi::class)
@Composable
fun WithConstraints(
    modifier: Modifier = Modifier,
    children: @Composable WithConstraintsScope.() -> Unit
) {
    SubcomposeLayout<Unit>(modifier) { constraints ->
        val scope = WithConstraintsScopeImpl(this, constraints, layoutDirection)
        val placeables = subcompose(Unit) { scope.children() }
            .fastMap { it.measure(constraints) }

        var maxWidth: Int = constraints.minWidth
        var maxHeight: Int = constraints.minHeight
        placeables.fastForEach {
            maxWidth = max(maxWidth, it.width)
            maxHeight = max(maxHeight, it.height)
        }

        layout(maxWidth, maxHeight) {
            placeables.fastForEach { it.place(0, 0) }
        }
    }
}

/**
 * Receiver scope being used by the children parameter of [WithConstraints]
 */
@Stable
interface WithConstraintsScope {
    /**
     * The constraints given by the parent layout in pixels.
     *
     * Use [minWidth], [maxWidth], [minHeight] or [maxHeight] if you need value in [Dp].
     */
    val constraints: Constraints
    /**
     * The current [LayoutDirection] to be used by this layout.
     */
    val layoutDirection: LayoutDirection
    /**
     * The minimum width in [Dp].
     *
     * @see constraints for the values in pixels.
     */
    val minWidth: Dp
    /**
     * The maximum width in [Dp].
     *
     * @see constraints for the values in pixels.
     */
    val maxWidth: Dp
    /**
     * The minimum height in [Dp].
     *
     * @see constraints for the values in pixels.
     */
    val minHeight: Dp
    /**
     * The minimum height in [Dp].
     *
     * @see constraints for the values in pixels.
     */
    val maxHeight: Dp
}

private data class WithConstraintsScopeImpl(
    private val density: Density,
    override val constraints: Constraints,
    override val layoutDirection: LayoutDirection
) : WithConstraintsScope {
    override val minWidth: Dp
        get() = with(density) { constraints.minWidth.toDp() }
    override val maxWidth: Dp
        get() = with(density) { constraints.maxWidth.toDp() }
    override val minHeight: Dp
        get() = with(density) { constraints.minHeight.toDp() }
    override val maxHeight: Dp
        get() = with(density) { constraints.maxHeight.toDp() }
}
