package edu.artic.map.carousel

import android.os.Bundle
import android.view.View
import com.fuzz.indicator.OffSetHint
import com.fuzz.rx.*
import com.jakewharton.rxbinding2.support.v4.view.pageSelections
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import edu.artic.adapter.itemChanges
import edu.artic.adapter.toPagerAdapter
import edu.artic.analytics.AnalyticsAction
import edu.artic.analytics.EventCategoryName
import edu.artic.analytics.ScreenName
import edu.artic.db.models.ArticObject
import edu.artic.db.models.ArticTour
import edu.artic.map.R
import edu.artic.media.audio.AudioPlayerService
import edu.artic.media.ui.getAudioServiceObservable
import edu.artic.viewmodel.BaseViewModelFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.fragment_tour_carousel.*
import kotlin.reflect.KClass

/**
 * Manages the interaction with map.
 * Houses the tour carousel component.
 * @author Sameer Dhakal (Fuzz)
 */
class TourCarouselFragment : BaseViewModelFragment<TourCarouselViewModel>() {

    override val viewModelClass: KClass<TourCarouselViewModel>
        get() = TourCarouselViewModel::class

    override val title = R.string.noTitle

    override val layoutResId: Int
        get() = R.layout.fragment_tour_carousel

    override val screenName: ScreenName?
        get() = null

    override val overrideStatusBarColor: Boolean
        get() = false

    private val adapter = TourCarouselAdapter()
    private var audioService: Subject<AudioPlayerService> = BehaviorSubject.create()
    private val tourObject: ArticTour by lazy { arguments!!.getParcelable<ArticTour>(ARG_TOUR_OBJECT) }

    override fun onRegisterViewModel(viewModel: TourCarouselViewModel) {
        viewModel.tourObject = tourObject
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAudioServiceObservable()
                .bindTo(audioService)
                .disposedBy(disposeBag)

        tourCarousel.adapter = adapter.toPagerAdapter()
        viewPagerIndicator.setViewPager(tourCarousel)
        viewPagerIndicator.setOffsetHints(OffSetHint.IMAGE_ALPHA)

        close.clicks()
                .defaultThrottle()
                .subscribe {
                    viewModel.leaveTour()
                }.disposedBy(disposeBag)

    }


    override fun setupBindings(viewModel: TourCarouselViewModel) {
        super.setupBindings(viewModel)

        viewModel.tourTitle
                .bindToMain(tourTitle.text())
                .disposedBy(disposeBag)

        viewModel.tourStopViewModels
                .bindToMain(adapter.itemChanges())
                .disposedBy(disposeBag)

        /**
         * Upon getting the player control command, get the mediaPlayer and pass it the command.
         */
        viewModel.playerControl
                .observeOn(AndroidSchedulers.mainThread())
                .withLatestFrom(audioService) { playControl, service ->
                    playControl to service
                }.subscribe { pair ->
                    val playControl = pair.first
                    val service = pair.second
                    when (playControl) {
                        is TourCarousalBaseViewModel.PlayerAction.Play -> {
                            var actionType = AnalyticsAction.playAudioTour;
                            when(playControl.type) {
                                TourCarousalBaseViewModel.Type.Stop -> actionType = AnalyticsAction.playAudioTourStop
                                TourCarousalBaseViewModel.Type.Overview -> actionType = AnalyticsAction.playAudioTour
                            }
                            if (playControl.audioFileModel != null) {
                                service.playPlayer(playControl.requestedObject, playControl.audioFileModel)
                                analyticsTracker.reportEvent(EventCategoryName.PlayAudio, actionType, playControl.audioFileModel.title!!)
                            } else {
                                service.playPlayer(playControl.requestedObject)
                                when(playControl.requestedObject) {
                                    is ArticObject -> analyticsTracker.reportEvent(EventCategoryName.PlayAudio, actionType, playControl.requestedObject.title)
                                }
                            }
                        }
                        is TourCarousalBaseViewModel.PlayerAction.Pause -> service.pausePlayer()
                    }
                }.disposedBy(disposeBag)


        audioService
                .flatMap { service -> service.audioPlayBackStatus }
                .bindTo(viewModel.audioPlayBackStatus)
                .disposedBy(disposeBag)

        audioService
                .flatMap { service -> service.currentTrack }
                .mapOptional()
                .bindTo(viewModel.currentTrack)
                .disposedBy(disposeBag)

        viewModel.currentPage
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { page ->
                    tourCarousel.setCurrentItem(page, true)
                }
                .disposedBy(disposeBag)

        tourCarousel.pageSelections()
                .skipInitialValue()
                .distinctUntilChanged()
                .bindTo(viewModel.currentPage)
                .disposedBy(disposeBag)
    }

    companion object {
        private val ARG_TOUR_OBJECT = "ARG_TOUR"

        fun create(tourObject: ArticTour): TourCarouselFragment {
            return TourCarouselFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TOUR_OBJECT, tourObject)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewPagerIndicator.setViewPager(null)
    }

}