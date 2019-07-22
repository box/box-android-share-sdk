package com.box.androidsdk.share.sharerepo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.*
import com.box.androidsdk.share.api.ShareController
import com.box.androidsdk.share.internal.models.BoxFeatures
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.*

import org.junit.*
import org.junit.rules.TestRule
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*

class ShareRepoTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()


    private val shareController: ShareController = mock()

    private val mockShareItem: BoxCollaborationItem = mock()
    private val mockEmailList: Array<String> = arrayOf("boxuser@box.com", "boxuser2@box.com")
    private val mockSelectedRole: BoxCollaboration.Role = BoxCollaboration.Role.EDITOR
    private val mockFilter: String = "filter"
    private val mockCollaboration: BoxCollaboration = mock()

    private val mockBoxFile: BoxFile = mock()

    private val canDownload = false
    private val newAccess = BoxSharedLink.Access.COMPANY
    private val newPassword = ""

    private val mockGetInviteeResponseTask: BoxFutureTask<BoxIteratorInvitees> = mock()
    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponseTask: BoxFutureTask<BoxCollaborationItem> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockAddCollabsResponseTask: BoxFutureTask<BoxResponseBatch> = mock()
    private val mockAddCollabsResponse: BoxResponse<BoxResponseBatch> = mock()
    private val mockFetchItemInfoResponseTask: BoxFutureTask<BoxItem> = mock()
    private val mockFetchItemInfoResponse: BoxResponse<BoxItem> = mock()

    private val mockSharedLinkResponseTask: BoxFutureTask<BoxItem> = mock()
    private val mockSharedLinkResponse: BoxResponse<BoxItem> = mock()

    private val mockFeatureResponseTask: BoxFutureTask<BoxFeatures> = mock()
    private val mockFeatureResponse: BoxResponse<BoxFeatures> = mock()

    private val mockDeleteCollaborationResponseTask: BoxFutureTask<BoxVoid> = mock()
    private val mockDeleteCollaborationResponse: BoxResponse<BoxVoid> = mock()

    private val mockUpdateOwnerResponseTask: BoxFutureTask<BoxVoid> = mock()
    private val mockUpdateOwnerResponse: BoxResponse<BoxVoid> = mock()

    private val mockUpdateCollaborationResponseTask: BoxFutureTask<BoxCollaboration> = mock()
    private val mockUpdateCollaborationResponse: BoxResponse<BoxCollaboration> = mock()

    private val mockFetchCollaborationResponseTask: BoxFutureTask<BoxIteratorCollaborations> = mock()
    private val mockFetchCollaborationResponse: BoxResponse<BoxIteratorCollaborations> = mock()

    private val mockDate: Date = mock()

    private lateinit var shareRepo: ShareRepo

    @Before
    fun setup() {
        whenever(shareController.fetchItemInfo(mockShareItem)).thenReturn(mockFetchItemInfoResponseTask)

        whenever(shareController.getInvitees(mockShareItem, mockFilter)).thenReturn(mockGetInviteeResponseTask)
        whenever(shareController.fetchRoles(mockShareItem)).thenReturn(mockFetchRolesResponseTask)
        whenever(shareController.addCollaborations(mockShareItem, mockSelectedRole, mockEmailList)).thenReturn(mockAddCollabsResponseTask)

        whenever(shareController.createDefaultSharedLink(mockShareItem)).thenReturn(mockSharedLinkResponseTask)
        whenever(shareController.disableShareLink(mockShareItem)).thenReturn(mockSharedLinkResponseTask)

        val mockBoxRequestsFile: BoxRequestsFile.UpdatedSharedFile = mock()
        whenever(shareController.getCreatedSharedLinkRequest(mockShareItem)).thenReturn(mock())
        whenever(shareController.getCreatedSharedLinkRequest(mockBoxFile)).thenReturn(mockBoxRequestsFile)
        whenever(shareController.executeRequest(BoxItem::class.java, (shareController.getCreatedSharedLinkRequest(mockBoxFile) as BoxRequestsFile.UpdatedSharedFile).setCanDownload(canDownload))).thenReturn(mockSharedLinkResponseTask)
        whenever(shareController.executeRequest(BoxItem::class.java, shareController.getCreatedSharedLinkRequest(mockShareItem).setAccess(newAccess))).thenReturn(mockSharedLinkResponseTask)
        whenever(shareController.executeRequest(BoxItem::class.java, shareController.getCreatedSharedLinkRequest(mockShareItem).setUnsharedAt(mockDate))).thenReturn(mockSharedLinkResponseTask)
        whenever(shareController.executeRequest(BoxItem::class.java, shareController.getCreatedSharedLinkRequest(mockShareItem).setRemoveUnsharedAtDate())).thenReturn(mockSharedLinkResponseTask)
        whenever(shareController.executeRequest(BoxItem::class.java, shareController.getCreatedSharedLinkRequest(mockShareItem).setPassword(newPassword))).thenReturn(mockSharedLinkResponseTask)
        whenever(shareController.supportedFeatures).thenReturn(mockFeatureResponseTask)

        whenever(shareController.fetchCollaborations(mockShareItem)).thenReturn(mockFetchCollaborationResponseTask)
        whenever(shareController.deleteCollaboration(mockCollaboration)).thenReturn(mockDeleteCollaborationResponseTask)
        whenever(shareController.updateCollaboration(mockCollaboration, mockSelectedRole)).thenReturn(mockUpdateCollaborationResponseTask)
        whenever(shareController.updateOwner(mockCollaboration)).thenReturn(mockUpdateOwnerResponseTask)


        shareRepo = ShareRepo(shareController)
        createStubs()
    }

    /**
     * Mock callback responses.
     */
    private fun createStubs() {
        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>
            callback.onCompleted(mockGetInviteeResponse)
            null
        }.whenever(mockGetInviteeResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>>())
        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxCollaborationItem>
            callback.onCompleted(mockFetchRolesResponse)
            null
        }.whenever(mockFetchRolesResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxCollaborationItem>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxResponseBatch>
            callback.onCompleted(mockAddCollabsResponse)
            null
        }.whenever(mockAddCollabsResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxResponseBatch>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxItem>
            callback.onCompleted(mockFetchItemInfoResponse)
            null
        }.whenever(mockFetchItemInfoResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxItem>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxItem>
            callback.onCompleted(mockSharedLinkResponse)
            null
        }.whenever(mockSharedLinkResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxItem>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations>
            callback.onCompleted(mockFetchCollaborationResponse)
            null
        }.whenever(mockFetchCollaborationResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxIteratorCollaborations>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxFeatures>
            callback.onCompleted(mockFeatureResponse)
            null
        }.whenever(mockFeatureResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxFeatures>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxCollaboration>
            callback.onCompleted(mockUpdateCollaborationResponse)
            null
        }.whenever(mockUpdateCollaborationResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxCollaboration>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxVoid>
            callback.onCompleted(mockUpdateOwnerResponse)
            null
        }.whenever(mockUpdateOwnerResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxVoid>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxVoid>
            callback.onCompleted(mockDeleteCollaborationResponse)
            null
        }.whenever(mockDeleteCollaborationResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxVoid>>())
    }


    @Test
    fun `test fetch roles update LiveData values correctly`() {
        assertNull(shareRepo.roleItem.value) //initially the LiveData should not have any value
        shareRepo.fetchRolesFromRemote(mockShareItem) //get a value and update as needed
        assertEquals(mockFetchRolesResponse, shareRepo.getRoleItem().value)
    }


    @Test
    fun `test fetch invitees update LiveData values correctly`() {
        assertNull(shareRepo.invitees.value) //initially the LiveData should not have any value
        shareRepo.fetchInviteesFromRemote(mockShareItem, mockFilter) //get a value and update as needed
        assertEquals(mockGetInviteeResponse, shareRepo.getInvitees().value)
    }

    @Test
    fun `test invite collabs update LiveData values correctly` () {
        assertNull(shareRepo.inviteCollabsBatchResponse.value) //initially the LiveData should not have any value
        shareRepo.inviteCollabs(mockShareItem, mockSelectedRole, mockEmailList) //get a value and update as needed
        assertEquals(mockAddCollabsResponse, shareRepo.getInviteCollabsBatchResponse().value)
    }

    @Test
    fun `test fetch item info update LiveData values correctly` () {
        assertNull(shareRepo.itemInfo.value) //initially the LiveData should not have any value
        shareRepo.fetchItemInfo(mockShareItem) //get a value and update as needed
        assertEquals(mockFetchItemInfoResponse, shareRepo.itemInfo.value)
    }

    @Test
    fun `test create shared link update LiveData values correctly` () {
        assertNull(shareRepo.shareLinkedItem.value) //initially the LiveData should not have any value

        shareRepo.createDefaultSharedLink(mockShareItem) //get a value and update as needed
        assertEquals(mockSharedLinkResponse, shareRepo.shareLinkedItem.value)
    }

    @Test
    fun `test disable shared link update LiveData values correctly` () {
        assertNull(shareRepo.shareLinkedItem.value) //initially the LiveData should not have any value

        shareRepo.disableSharedLink(mockShareItem) //get a value and update as needed
        assertEquals(mockSharedLinkResponse, shareRepo.shareLinkedItem.value)
    }

    @Test
    fun `test change download permission update LiveData values correctly` () {
        assertNull(shareRepo.shareLinkedItem.value) //initially the LiveData should not have any value

        shareRepo.changeDownloadPermission(mockBoxFile, canDownload) //get a value and update as needed
        assertEquals(mockSharedLinkResponse, shareRepo.shareLinkedItem.value)
    }

    @Test
    fun `test change download permission illegal argument` () {
        assertNull(shareRepo.shareLinkedItem.value) //initially the LiveData should not have any value
        try {
            shareRepo.changeDownloadPermission(mockShareItem, canDownload)
        } catch (e: Exception) {
            assertTrue(e is IllegalArgumentException) //illegal argument exception should occur
        }
    }

    @Test
    fun `test change access level update LiveData values correctly` () {
        assertNull(shareRepo.shareLinkedItem.value) //initially the LiveData should not have any value

        shareRepo.changeAccessLevel(mockBoxFile, newAccess) //get a value and update as needed
        assertEquals(mockSharedLinkResponse, shareRepo.shareLinkedItem.value)
    }


    @Test
    fun `test set expiry date update LiveData values correctly` () {
        assertNull(shareRepo.shareLinkedItem.value) //initially the LiveData should not have any value

        shareRepo.setExpiryDate(mockBoxFile, mockDate) //get a value and update as needed
        assertEquals(mockSharedLinkResponse, shareRepo.shareLinkedItem.value)
    }


    @Test
    fun `test remove expiry date update LiveData values correctly` () {
        assertNull(shareRepo.shareLinkedItem.value) //initially the LiveData should not have any value

        shareRepo.removeExpiryDate(mockBoxFile) //get a value and update as needed
        assertEquals(mockSharedLinkResponse, shareRepo.shareLinkedItem.value)
    }

    @Test
    fun `test change password update LiveData values correctly` () {
        assertNull(shareRepo.shareLinkedItem.value) //initially the LiveData should not have any value

        shareRepo.changePassword(mockShareItem, newPassword) //get a value and update as needed
        assertEquals(mockSharedLinkResponse, shareRepo.shareLinkedItem.value)
    }

    @Test
    fun `test fetch features update LiveData values correctly` () {
        assertNull(shareRepo.supportFeatures.value) //initially the LiveData should not have any value

        shareRepo.fetchSupportedFeatures() //get a value and update as needed
        assertEquals(mockFeatureResponse, shareRepo.supportFeatures.value)
    }

    @Test
    fun `test fetch collaborations update LiveData values correctly` () {
        assertNull(shareRepo.collaborations.value) //initially the LiveData should not have any value

        shareRepo.fetchCollaborations(mockShareItem) //get a value and update as needed
        assertEquals(mockFetchCollaborationResponse, shareRepo.collaborations.value)
    }

    @Test
    fun `test update collaborations update LiveData values correctly` () {
        assertNull(shareRepo.updateCollaboration.value) //initially the LiveData should not have any value

        shareRepo.updateCollaboration(mockCollaboration, mockSelectedRole) //get a value and update as needed
        assertEquals(mockUpdateCollaborationResponse, shareRepo.updateCollaboration.value)
    }

    @Test
    fun `test update owner update LiveData values correctly` () {
        assertNull(shareRepo.updateOwner.value) //initially the LiveData should not have any value

        shareRepo.updateOwner(mockCollaboration) //get a value and update as needed
        assertEquals(mockUpdateOwnerResponse, shareRepo.updateOwner.value)
    }

    @Test
    fun `test delete collaboration update LiveData values correctly` () {
        assertNull(shareRepo.deleteCollaboration.value) //initially the LiveData should not have any value

        shareRepo.deleteCollaboration(mockCollaboration) //get a value and update as needed
        assertEquals(mockDeleteCollaborationResponse, shareRepo.deleteCollaboration.value)
    }



}