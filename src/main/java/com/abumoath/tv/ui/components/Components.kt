package com.abumoath.tv.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.abumoath.tv.R
import com.abumoath.tv.data.model.ChannelCard
import com.abumoath.tv.data.model.TitleCard
import com.abumoath.tv.ui.theme.Brand
import com.abumoath.tv.util.Format

private val CardShape = RoundedCornerShape(10.dp)

/** Clickable TV surface with a clear focus state: gold border + gentle zoom (cheap on Mali-450). */
@Composable
fun FocusCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = CardShape,
    focusedScale: Float = 1.07f,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Brand.Surface2,
            focusedContainerColor = Brand.Surface2,
            pressedContainerColor = Brand.Surface2
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = focusedScale),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(3.dp, Brand.Gold), shape = shape)
        ),
        glow = ClickableSurfaceDefaults.glow(),
        content = content
    )
}

@Composable
fun QualityBadge(q: String?, modifier: Modifier = Modifier) {
    val label = Format.qualityLabel(q) ?: return
    Box(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (q == "FHD") Brand.Gold else Brand.RoyalBlue)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (q == "FHD") Color.Black else Color.White)
    }
}

/** Poster card (2:3) with the title under the image. */
@Composable
fun PosterCard(item: TitleCard, onClick: () -> Unit, modifier: Modifier = Modifier, width: Dp = 140.dp) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier.width(width)) {
        FocusCard(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f).onFocusChanged { focused = it.isFocused }) {
            AsyncImage(
                model = item.posterUrl ?: item.backdropUrl,
                contentDescription = item.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (item.posterUrl == null && item.backdropUrl == null) {
                Text(item.displayTitle, Modifier.align(Alignment.Center).padding(8.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall)
            }
            QualityBadge(item.quality, Modifier.align(Alignment.TopEnd).padding(6.dp))
            item.progress?.let { ProgressLine(it.fraction, Modifier.align(Alignment.BottomCenter)) }
        }
        Text(
            item.displayTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = if (focused) Brand.GoldLight else Brand.Text,
            modifier = Modifier.padding(top = 6.dp)
        )
        item.year?.let { Text(it.toString(), style = MaterialTheme.typography.labelSmall, color = Brand.Muted) }
    }
}

/** 16:9 card used for "continue watching" and episodes. */
@Composable
fun WideCard(title: String, subtitle: String?, imageUrl: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier, width: Dp = 260.dp) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier.width(width)) {
        FocusCard(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).onFocusChanged { focused = it.isFocused }) {
            AsyncImage(model = imageUrl, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (progress != null) ProgressLine(progress, Modifier.align(Alignment.BottomCenter))
        }
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall,
            color = if (focused) Brand.GoldLight else Brand.Text, modifier = Modifier.padding(top = 6.dp))
        if (subtitle != null) Text(subtitle, maxLines = 1, style = MaterialTheme.typography.labelSmall, color = Brand.Muted)
    }
}

@Composable
fun ProgressLine(fraction: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(5.dp).background(Color.Black.copy(alpha = 0.6f))) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().background(Brand.Gold))
    }
}

/** Live channel card: logo on a dark tile + name. */
@Composable
fun ChannelTile(item: ChannelCard, onClick: () -> Unit, modifier: Modifier = Modifier, width: Dp = 180.dp) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier.width(width)) {
        FocusCard(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).onFocusChanged { focused = it.isFocused }) {
            Box(Modifier.fillMaxSize().background(Brush_tile).padding(14.dp), contentAlignment = Alignment.Center) {
                if (item.logoUrl != null) {
                    AsyncImage(model = item.logoUrl, contentDescription = item.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                } else {
                    Text(item.name, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                }
            }
            Box(Modifier.align(Alignment.TopStart).padding(6.dp).clip(RoundedCornerShape(4.dp)).background(Brand.Live).padding(horizontal = 6.dp, vertical = 1.dp)) {
                Text(stringResource(R.string.live_badge), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            QualityBadge(item.quality, Modifier.align(Alignment.TopEnd).padding(6.dp))
        }
        Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall,
            color = if (focused) Brand.GoldLight else Brand.Text, modifier = Modifier.padding(top = 6.dp))
    }
}

private val Brush_tile = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF1C2233), Color(0xFF0E1119)))

/** Horizontal content row with a title. Restores focus to the last focused item when navigating back. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> ContentRow(
    title: String,
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {
    Column(modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 48.dp, end = 48.dp, bottom = 10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Brand.Text)
        }
        LazyRow(
            modifier = Modifier.focusRestorer(),
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(items, key = key) { itemContent(it) }
            if (onSeeAll != null) {
                item(key = "see_all") {
                    FocusCard(onClick = onSeeAll, modifier = Modifier.size(140.dp, 210.dp)) {
                        Text(stringResource(R.string.see_all), Modifier.align(Alignment.Center), color = Brand.GoldLight, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

/** Primary action button in brand gold. */
@Composable
fun GoldButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, @DrawableRes icon: Int? = null, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.colors(
            containerColor = Brand.Surface2,
            contentColor = Brand.Text,
            focusedContainerColor = Brand.Gold,
            focusedContentColor = Color.Black
        )
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** A selectable option row (used in menus, filters and player settings). */
@Composable
fun OptionRow(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, trailing: String? = null) {
    FocusCard(onClick = onClick, modifier = modifier.fillMaxWidth().height(52.dp), focusedScale = 1.02f, shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(if (selected) Brand.Gold else Color.Transparent))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = if (selected) Brand.GoldLight else Brand.Text, modifier = Modifier.weight(1f), maxLines = 1)
            if (trailing != null) Text(trailing, style = MaterialTheme.typography.bodySmall, color = Brand.Muted)
        }
    }
}

/** Small pill used for filters (country, genre…). */
@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Brand.GoldDark else Brand.Surface2,
            contentColor = if (selected) Brand.GoldLight else Brand.Text,
            focusedContainerColor = Brand.Gold,
            focusedContentColor = Color.Black
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Box(Modifier.fillMaxHeight().padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.brand_logo), contentDescription = null, modifier = Modifier.size(96.dp))
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.loading), color = Brand.Muted)
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 600.dp)) {
            Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, color = Brand.Text)
            if (onRetry != null) {
                Spacer(Modifier.height(18.dp))
                val fr = remember { FocusRequester() }
                GoldButton(stringResource(R.string.retry), onRetry, Modifier.focusRequester(fr))
                LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }
            }
        }
    }
}

@Composable
fun EmptyView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.empty_list), color = Brand.Muted, style = MaterialTheme.typography.titleMedium)
    }
}

