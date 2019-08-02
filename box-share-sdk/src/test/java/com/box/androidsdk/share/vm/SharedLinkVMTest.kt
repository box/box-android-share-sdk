package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.content.models.BoxItem
import com.box.androidsdk.content.models.BoxSharedLink
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.internal.models.BoxFeatures
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.sharerepo.ShareRepo
import com.box.androidsdk.share.utils.ShareSDKTransformer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.sql.Date
import java.util.ArrayList

class SharedLinkVMTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val mockShareItem: BoxCollaborationItem = mock()
    private val mockShareRepo: ShareRepo = mock()
    private val mockTransformer: ShareSDKTransformer = mock()

    private val mShareLinkedItem: LiveData<PresenterData<BoxItem>> = mock()
    private val mSupportedFeatures: LiveData<PresenterData<BoxFeatures>> = mock()


    private val mockShareOperationResponse: BoxResponse<BoxItem> = mock()
    private val mockSupportedFeaturesResponse: BoxResponse<BoxFeatures> = mock()

    private val mockShareOperationTransformedResponse: PresenterData<BoxItem> = mock()
    private val mockSupportedFeaturesTransformedResponse: PresenterData<BoxFeatures> = mock()


    private val downloadPermission = false
    private val date: Date = mock()
    private val accessLevel = BoxSharedLink.Access.COMPANY
    private val password = ""

    private lateinit var shareLinkVM: SharedLinkVM
    @Before
    fun setup() {
        mockShareRepo()
        mockTransformer()
        shareLinkVM = SharedLinkVM(mockShareRepo, mockShareItem, mockTransformer)

        attachObservers()
    }

    private fun mockShareRepo() {
        whenever(mockShareRepo.shareLinkedItem).thenReturn(MutableLiveData())
        whenever(mockShareRepo.supportFeatures).thenReturn(MutableLiveData())

        whenever(mockShareRepo.createDefaultSharedLink(mockShareItem)).then {
            val data = mockShareRepo.shareLinkedItem as MutableLiveData
            data.postValue(mockShareOperationResponse)
        }

        whenever(mockShareRepo.changeDownloadPermission(mockShareItem, downloadPermission)).then {
            val data = mockShareRepo.shareLinkedItem as MutableLiveData
            data.postValue(mockShareOperationResponse)
        }

        whenever(mockShareRepo.disableSharedLink(mockShareItem)).then {
            val data = mockShareRepo.shareLinkedItem as MutableLiveData
            data.postValue(mockShareOperationResponse)
        }

        whenever(mockShareRepo.setExpiryDate(mockShareItem, date)).then {
            val data = mockShareRepo.shareLinkedItem as MutableLiveData
            data.postValue(mockShareOperationResponse)
        }

        whenever(mockShareRepo.changeAccessLevel(mockShareItem, accessLevel)).then {
            val data = mockShareRepo.shareLinkedItem as MutableLiveData
            data.postValue(mockShareOperationResponse)
        }

        whenever(mockShareRepo.changePassword(mockShareItem, password)).then {
            val data = mockShareRepo.shareLinkedItem as MutableLiveData
            data.postValue(mockShareOperationResponse)
        }

        whenever(mockShareRepo.removeExpiryDate(mockShareItem)).then {
            val data = mockShareRepo.shareLinkedItem as MutableLiveData
            data.postValue(mockShareOperationResponse)
        }

        whenever(mockShareRepo.fetchSupportedFeatures()).then {
            val data = mockShareRepo.supportFeatures as MutableLiveData
            data.postValue(mockSupportedFeaturesResponse)
        }
    }

    private fun mockTransformer() {
        whenever(mockTransformer.getSharedLinkItemPresenterData(mockShareOperationResponse, mockShareItem)).thenReturn(mockShareOperationTransformedResponse)
        whenever(mockTransformer.getSupportedFeaturePresenterData(mockSupportedFeaturesResponse)).thenReturn(mockSupportedFeaturesTransformedResponse)
    }

    private fun attachObservers() {
        shareLinkVM.sharedLinkedItem.observeForever(mock())
        shareLinkVM.supportedFeatures.observeForever(mock())
    }

    @Test
    fun `test create share link update shared linked item with transformed data on repo's live data change`() {
        assertEquals(null, shareLinkVM.sharedLinkedItem.value)

        //Make a network call which will trigger repo's live data change
        shareLinkVM.createDefaultSharedLink(mockShareItem)

        assertEquals(mockShareOperationTransformedResponse, shareLinkVM.sharedLinkedItem.value)
    }


    @Test
    fun `test disable share link update shared linked item with transformed data on repo's live data change`() {
        assertEquals(null, shareLinkVM.sharedLinkedItem.value)

        //Make a network call which will trigger repo's live data change
        shareLinkVM.disableSharedLink(mockShareItem)

        assertEquals(mockShareOperationTransformedResponse, shareLinkVM.sharedLinkedItem.value)
    }


    @Test
    fun `test change link's download permission update shared linked item with transformed data on repo's live data change`() {
        assertEquals(null, shareLinkVM.sharedLinkedItem.value)

        //Make a network call which will trigger repo's live data change
        shareLinkVM.changeDownloadPermission(mockShareItem, downloadPermission)

        assertEquals(mockShareOperationTransformedResponse, shareLinkVM.sharedLinkedItem.value)
    }


    @Test
    fun `test set link's expiry date update shared linked item with transformed data on repo's live data change`() {
        assertEquals(null, shareLinkVM.sharedLinkedItem.value)

        //Make a network call which will trigger repo's live data change
        shareLinkVM.setExpiryDate(mockShareItem, date)

        assertEquals(mockShareOperationTransformedResponse, shareLinkVM.sharedLinkedItem.value)
    }

    @Test
    fun `test remove link's expiry date update shared linked item with transformed data on repo's live data change`() {
        assertEquals(null, shareLinkVM.sharedLinkedItem.value)

        //Make a network call which will trigger repo's live data change
        shareLinkVM.removeExpiryDate(mockShareItem)

        assertEquals(mockShareOperationTransformedResponse, shareLinkVM.sharedLinkedItem.value)
    }

    @Test
    fun `test change link's password update shared linked item with transformed data on repo's live data change`() {
        assertEquals(null, shareLinkVM.sharedLinkedItem.value)

        //Make a network call which will trigger repo's live data change
        shareLinkVM.changePassword(mockShareItem, password)

        assertEquals(mockShareOperationTransformedResponse, shareLinkVM.sharedLinkedItem.value)
    }

    @Test
    fun `test change link's access level update shared linked item with transformed data on repo's live data change`() {
        assertEquals(null, shareLinkVM.sharedLinkedItem.value)

        //Make a network call which will trigger repo's live data change
        shareLinkVM.changeAccessLevel(mockShareItem, accessLevel)

        assertEquals(mockShareOperationTransformedResponse, shareLinkVM.sharedLinkedItem.value)
    }

    @Test
    fun `test get supported features update supported feature with transformed data on repo's live data change`() {
        assertEquals(null, shareLinkVM.supportedFeatures.value)

        //Make a network call which will trigger repo's live data change
        shareLinkVM.fetchSupportedFeatures()

        assertEquals(mockSupportedFeaturesTransformedResponse, shareLinkVM.supportedFeatures.value)
    }


    @Test
    fun `test get active radio button null`() {
        whenever(mockShareItem.allowedSharedLinkAccessLevels).thenReturn(null)

        //Make a network call which will trigger repo's live data change

        assertEquals(0, shareLinkVM.activeRadioButtons.size)
    }

    @Test
    fun `test get active radio button 2 item active`() {
        val list = arrayListOf<BoxSharedLink.Access>()
        list.add(BoxSharedLink.Access.COMPANY)
        list.add(BoxSharedLink.Access.COLLABORATORS)
        whenever(mockShareItem.allowedSharedLinkAccessLevels).thenReturn(list)

        //Make a network call which will trigger repo's live data change

        assertEquals(2, shareLinkVM.activeRadioButtons.size)
    }
}