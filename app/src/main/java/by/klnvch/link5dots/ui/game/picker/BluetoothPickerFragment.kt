/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package by.klnvch.link5dots.ui.game.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.imageResource
import androidx.lifecycle.ViewModelProvider
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.game.OfflineGameViewModel
import by.klnvch.link5dots.ui.game.OnlineGameViewModel
import by.klnvch.link5dots.ui.theme.AppTheme
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class BluetoothPickerFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val gameViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[OfflineGameViewModel.KEY, OnlineGameViewModel::class.java]

        val bluetoothViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[VisibilityViewModel.KEY, VisibilityViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    Box(
                        modifier = Modifier
                            .background(
                                ShaderBrush(
                                    ImageShader(
                                        ImageBitmap.imageResource(R.drawable.paper),
                                        TileMode.Repeated,
                                        TileMode.Repeated
                                    )
                                )
                            )
                    ) {
                        BluetoothPickerScreen(gameViewModel, bluetoothViewModel)
                    }
                }
            }
        }
    }
}
