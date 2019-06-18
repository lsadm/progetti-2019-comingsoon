package it.unicampania.cinemap

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.utils.StorageUtils
import info.movito.themoviedbapi.TmdbApi
import it.unicampania.util.onDefault
import it.unicampania.util.onIO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    lateinit var tmdbApiDeferred: Deferred<TmdbApi>
    var job: Job = Job()

    lateinit var imageLoader: ImageLoader

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = GlobalScope.launch {
            onIO {
                tmdbApiDeferred = async { TmdbApi(BuildConfig.THE_MOVIE_DB_API_TOKEN) }
                imageLoader = onDefault {
                    ImageLoader.getInstance().apply {
                        init(
                            ImageLoaderConfiguration.Builder(this@MainActivity)
                                .memoryCache(LruMemoryCache(2 * 1024 * 1024))
                                .diskCache(UnlimitedDiskCache(StorageUtils.getCacheDirectory(this@MainActivity)))
                                .defaultDisplayImageOptions(
                                    DisplayImageOptions.Builder()
                                        .showImageOnFail(R.drawable.ic_error_black_24dp)
                                        .showImageForEmptyUri(R.drawable.ic_error_black_24dp)
                                        .cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .build()
                                )
                                .build()
                        )
                    }
                }
            }
        }
        setContentView(R.layout.activity_main)
        bottomNavigationView.setupWithNavController(Navigation.findNavController(this, R.id.navHost))
    }
}