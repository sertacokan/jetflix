package com.yasinkacmaz.jetflix.ui.movies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.insets.LocalWindowInsets
import com.yasinkacmaz.jetflix.R
import com.yasinkacmaz.jetflix.ui.common.error.ErrorColumn
import com.yasinkacmaz.jetflix.ui.common.error.ErrorRow
import com.yasinkacmaz.jetflix.ui.common.loading.LoadingColumn
import com.yasinkacmaz.jetflix.ui.common.loading.LoadingRow
import com.yasinkacmaz.jetflix.ui.movies.movie.Movie
import com.yasinkacmaz.jetflix.ui.movies.movie.MovieContent
import com.yasinkacmaz.jetflix.ui.navigation.LocalNavigator
import com.yasinkacmaz.jetflix.ui.navigation.Screen.MovieDetail
import com.yasinkacmaz.jetflix.util.toDp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val CELL_COUNT = 2
private val GRID_SPACING = 8.dp

@OptIn(ExperimentalFoundationApi::class)
private val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(CELL_COUNT) }

@Composable
fun MoviesGrid() {
    val moviesViewModel = viewModel<MoviesViewModel>()
    val movies = moviesViewModel.movies.collectAsLazyPagingItems()
    val state = rememberLazyListState()
    val navigator = LocalNavigator.current
    val onMovieClicked: (Int) -> Unit = { movieId ->
        navigator.navigateTo(MovieDetail(movieId))
    }
    LaunchedEffect(Unit) {
        moviesViewModel.filterStateChanges
            .onEach {
                state.scrollToItem(0)
                movies.refresh()
            }
            .launchIn(this)
    }

    when (movies.loadState.refresh) {
        is LoadState.Loading -> {
            LoadingColumn(stringResource(id = R.string.fetching_movies))
        }
        is LoadState.Error -> {
            val error = movies.loadState.refresh as LoadState.Error
            ErrorColumn(error.error.message.orEmpty())
        }
        else -> {
            LazyMoviesGrid(state, movies, onMovieClicked)
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun LazyMoviesGrid(
    state: LazyListState,
    moviePagingItems: LazyPagingItems<Movie>,
    onMovieClicked: (Int) -> Unit
) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(CELL_COUNT),
        contentPadding = PaddingValues(
            start = GRID_SPACING,
            end = GRID_SPACING,
            bottom = LocalWindowInsets.current.navigationBars.bottom.toDp().dp.plus(GRID_SPACING)
        ),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING, Alignment.CenterHorizontally),
        state = state,
        content = {
            items(moviePagingItems.itemCount) { index ->
                val movie = moviePagingItems.peek(index) ?: return@items
                MovieContent(
                    movie,
                    Modifier
                        .height(320.dp)
                        .padding(vertical = GRID_SPACING),
                    onMovieClicked
                )
            }
            renderLoading(moviePagingItems.loadState)
            renderError(moviePagingItems.loadState)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyGridScope.renderLoading(loadState: CombinedLoadStates) {
    if (loadState.append !is LoadState.Loading) return

    item(span = span) {
        val title = stringResource(R.string.fetching_more_movies)
        LoadingRow(title = title, modifier = Modifier.padding(vertical = GRID_SPACING))
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyGridScope.renderError(loadState: CombinedLoadStates) {
    val message = (loadState.append as? LoadState.Error)?.error?.message ?: return

    item(span = span) {
        ErrorRow(title = message, modifier = Modifier.padding(vertical = GRID_SPACING))
    }
}
