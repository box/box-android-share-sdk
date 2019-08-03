package com.box.androidsdk.share.utils;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.*
import com.box.androidsdk.share.R
import com.box.androidsdk.share.internal.models.BoxFeatures
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createBoxCollaborationResponseBatch
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createFailedBoxCollaborationResponse
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createFailedBoxItemResponse
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createFailedBoxIterCollaborationResponse
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createSuccessfulBoxItemResponse
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createSuccessfulBoxCollaborationResponse
import com.box.androidsdk.share.vm.PresenterData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test;
import org.junit.rules.TestRule
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import javax.net.ssl.HttpsURLConnection

class ShareSDKTransformerTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val mockFailedToAddException: BoxException = mock()
    private val mockHttpNotModifiedException: BoxException = mock()
    private val mockHttpForbiddenException: BoxException = mock()
    private val mockBoxNetworkErrorException: BoxException = mock()
    private val mockAlreadyCollabException: BoxException = mock()
    private val mockBadRequestException: BoxException = mock()
    private val mockBoxGenericException: BoxException = mock()

    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockInviteCollabsResponse: BoxResponse<BoxResponseBatch> = mock()

    private val mockGetInviteesResult: BoxIteratorInvitees = mock()
    private val mockFetchRoleItemResult: BoxCollaborationItem = mock()
    private lateinit var mockInviteCollabsResult: BoxResponseBatch

    private val mockGenericException: Exception = mock()
    
    private val shareSDKTransformer = ShareSDKTransformer()


    @Before
    fun setup() {
        createExceptions()
    }

    private fun createExceptions() {

        whenever(mockHttpForbiddenException.responseCode).thenReturn(HttpsURLConnection.HTTP_FORBIDDEN)
        whenever(mockBoxNetworkErrorException.errorType).thenReturn(BoxException.ErrorType.NETWORK_ERROR)

        val boxErrorAlreadyAdded: BoxError = mock()
        whenever(boxErrorAlreadyAdded.code).thenReturn(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR)
        whenever(mockAlreadyCollabException.asBoxError).thenReturn(boxErrorAlreadyAdded)
        whenever(mockAlreadyCollabException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)

        val boxErrorFailedToAdd: BoxError = mock()
        whenever(boxErrorFailedToAdd.code).thenReturn("")
        whenever(mockFailedToAddException.asBoxError).thenReturn(boxErrorFailedToAdd)
        whenever(mockFailedToAddException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)
        whenever(mockBadRequestException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)

        whenever(mockHttpNotModifiedException.responseCode).thenReturn(HttpsURLConnection.HTTP_NOT_MODIFIED)

        whenever(mockBoxGenericException.errorType).thenReturn(BoxException.ErrorType.OTHER)
    }

    @Test
    fun `test fetch role success`() {
        //configs
        whenever(mockFetchRolesResponse.isSuccess).thenReturn(true)
        whenever(mockFetchRolesResponse.result).thenReturn(mockFetchRoleItemResult)

        //make a network call to fetch roles
        val result = shareSDKTransformer.getFetchRolesItemPresenterData(mockFetchRolesResponse)

        assertEquals(true, result?.isSuccess)
        assertEquals(mockFetchRoleItemResult, result?.data)
    }

    @Test
    fun `test fetch role failure`() {
        //configs
        whenever(mockFetchRolesResponse.isSuccess).thenReturn(false)
        val exception: BoxException = mock()
        whenever(mockFetchRolesResponse.exception).thenReturn(exception)
        //make a network call to fetch roles
        val result = shareSDKTransformer.getFetchRolesItemPresenterData(mockFetchRolesResponse)

        assertEquals(false, result?.isSuccess)
        assertEquals(R.string.box_sharesdk_network_error, result?.strCode)
    }

    @Test
    fun `test get invitees success`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(true)
        whenever(mockGetInviteeResponse.result).thenReturn(mockGetInviteesResult)

        //make a network call to fetch roles
        val result = shareSDKTransformer.getInviteesPresenterData(mockGetInviteeResponse)

        assertEquals(true, result?.isSuccess)
        assertEquals(mockGetInviteesResult, result?.data)
    }

    @Test
    fun `test get invitees failure http forbidden`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockHttpForbiddenException)

        //make a network call to fetch roles
        val result = shareSDKTransformer.getInviteesPresenterData(mockGetInviteeResponse)

        assertEquals(false, result?.isSuccess)
        assertEquals(R.string.box_sharesdk_insufficient_permissions, result?.strCode)
    }

    @Test
    fun `test get invitees failure box network error`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockBoxNetworkErrorException)

        //make a network call to fetch roles
        val result = shareSDKTransformer.getInviteesPresenterData(mockGetInviteeResponse)

        assertEquals(false, result?.isSuccess)
        assertEquals(R.string.box_sharesdk_network_error, result?.strCode)
    }


    @Test
    fun `test get invite collabs presenter data for successful request where response size is 1 and accessible by is not null`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createSuccessfulBoxCollaborationResponse(dummyName)

        val boxResponses = createBoxCollaborationResponseBatch(boxResponse)

        //process request
        val result = shareSDKTransformer.getPresenterDataForSuccessfulRequest(boxResponses)

        //compare values
        assertEquals(R.string.box_sharesdk_collaborator_invited, result.strCode)
        assertEquals(dummyName, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test get invite collabs presenter data for successful request where response size is 1 and accessible by is null`() {
        //configs
        val dummyName = null
        val boxResponse = createSuccessfulBoxCollaborationResponse(dummyName)

        val boxResponses = createBoxCollaborationResponseBatch(boxResponse)

        //process request
        val result = shareSDKTransformer.getPresenterDataForSuccessfulRequest(boxResponses)


        //compare values
        assertEquals(R.string.box_sharesdk_a_collaborator_invited, result.strCode)
        assertEquals(null, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test get invite collabs presenter data for successful request where response size is greater than 1`() {
        //configs
        val dummyName = "Box User"
        val boxResponse: BoxResponse<BoxCollaboration> = createSuccessfulBoxCollaborationResponse(dummyName)
        val boxResponse2: BoxResponse<BoxCollaboration> = createSuccessfulBoxCollaborationResponse(dummyName+"2")
        val boxResponses = createBoxCollaborationResponseBatch(boxResponse, boxResponse2)

        //process request
        val result = shareSDKTransformer.getPresenterDataForSuccessfulRequest(boxResponses)

        //compare values
        assertEquals(R.string.box_sharesdk_collaborators_invited, result.strCode)
        assertEquals(null, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test get invite collabs presenter data for failed request where failedCollabsList is not empty`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 1
        val failedCollabs = arrayListOf<String>()
        failedCollabs.add("failure 1")
        failedCollabs.add("failure 2")
        val res = "failure 1 failure 2"


        //process request
        val result = shareSDKTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_following_collaborators_error, result.strCode)
        assertEquals(res, result.data)
        assertEquals(false, result.isSuccess)
        assertEquals(true, result.isSnackBarMessage)
        assertEquals(1, result.alreadyAdddedCount)

    }

    @Test
    fun `test get invite collabs presenter data for failed request where failedCollabsList is empty and alreadyAddedCount is 1`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 1
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = shareSDKTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_has_already_been_invited, result.strCode)
        assertEquals(dummyName, result.data)
        assertEquals(true, result.isSuccess) //request failing only due to adding already added collabs is still considered a success
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(1, result.alreadyAdddedCount)

    }

    @Test
    fun `test get invite collabs presenter data for failed request where failedCollabsList is empty alreadyAddedCount is greater than 1`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 2
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = shareSDKTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_num_has_already_been_invited, result.strCode)
        assertEquals("2", result.data)
        assertEquals(true, result.isSuccess) //request failing only due to adding already added collabs is still considered a success
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(2, result.alreadyAdddedCount)

    }

    @Test
    fun `test get invite collabs presenter data for failed request where failedCollabsList is empty alreadyAddedCount is 0`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 0
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = shareSDKTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_unable_to_invite, result.strCode)
        assertEquals(null, result.data)
        assertEquals(false, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)

    }

    @Test
    fun `test get invite presenter data where all requests succeed`() {
        //configs
        val boxResponse = createSuccessfulBoxCollaborationResponse("user1")
        val boxResponse2 = createSuccessfulBoxCollaborationResponse("user2")
        val boxResponse3 = createSuccessfulBoxCollaborationResponse("user3")
        mockInviteCollabsResult = createBoxCollaborationResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = shareSDKTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

        assertEquals(R.string.box_sharesdk_collaborators_invited, result.strCode)
        assertEquals(null, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test add collab 1 succeed no failure`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createSuccessfulBoxCollaborationResponse(dummyName)
        mockInviteCollabsResult = createBoxCollaborationResponseBatch(boxResponse)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = shareSDKTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

        assertEquals(R.string.box_sharesdk_collaborator_invited, result.strCode)
        assertEquals(dummyName, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test add collab succeed 1 already added 1`() {
        //configs
        val dummyName = "user2"
        val boxResponse = createSuccessfulBoxCollaborationResponse("user1")
        val boxResponse2 = createFailedBoxCollaborationResponse(dummyName, mockAlreadyCollabException)
        mockInviteCollabsResult = createBoxCollaborationResponseBatch(boxResponse, boxResponse2)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = shareSDKTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

        assertEquals(R.string.box_sharesdk_has_already_been_invited, result.strCode)
        assertEquals(dummyName, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(1, result.alreadyAdddedCount)
    }

    @Test
    fun `test add collab succeed 1 already add 2`() {
        //configs
        val boxResponse = createSuccessfulBoxCollaborationResponse("user1")
        val boxResponse2 = createFailedBoxCollaborationResponse("user2", mockAlreadyCollabException)
        val boxResponse3 = createFailedBoxCollaborationResponse("user3", mockAlreadyCollabException)
        mockInviteCollabsResult= createBoxCollaborationResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = shareSDKTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

        assertEquals(R.string.box_sharesdk_num_has_already_been_invited, result.strCode)
        assertEquals("2", result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(2, result.alreadyAdddedCount)
    }

    @Test
    fun `test add collab succeed 1 failed to add 1 already added 1`() {
        //configs
        val failedName = "user2"
        val boxResponse = createSuccessfulBoxCollaborationResponse("user1")
        val boxResponse2 = createFailedBoxCollaborationResponse(failedName, mockFailedToAddException)
        val boxResponse3 = createFailedBoxCollaborationResponse("user3", mockAlreadyCollabException)
        mockInviteCollabsResult = createBoxCollaborationResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = shareSDKTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

        assertEquals(R.string.box_sharesdk_following_collaborators_error, result.strCode)
        assertEquals(failedName, result.data)
        assertEquals(false, result.isSuccess)
        assertEquals(true, result.isSnackBarMessage)
        assertEquals(1, result.alreadyAdddedCount)
    }

    @Test
    fun `test add collab succeed 1 failed to add 2`() {
        //configs
        val failedName1 = "user2"
        val failedName2 = "user3"
        val boxResponse = createSuccessfulBoxCollaborationResponse("user1")
        val boxResponse2 = createFailedBoxCollaborationResponse(failedName1, mockFailedToAddException)
        val boxResponse3 = createFailedBoxCollaborationResponse(failedName2, mockFailedToAddException)
        mockInviteCollabsResult = createBoxCollaborationResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = shareSDKTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)


        assertEquals(R.string.box_sharesdk_following_collaborators_error, result.strCode)
        assertEquals("$failedName1 $failedName2", result.data)
        assertEquals(false, result.isSuccess)
        assertEquals(true, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test shared link transformer success not instance of BoxRequestItem`() {
        //configs
        val boxResponse = createSuccessfulBoxItemResponse()
        val result = shareSDKTransformer.getSharedLinkItemPresenterData(boxResponse, mock())

        assertEquals(true, result.isSuccess)
        assertEquals(null, result.data)
    }

    @Test
    fun `test shared link transformer success instance of BoxRequestItem`() {
        //configs
        val boxResponse = createSuccessfulBoxItemResponse()
        val boxRequestItem: BoxRequestItem<*, *> = mock()
        whenever(boxResponse.request).thenReturn(boxRequestItem)

        val result = shareSDKTransformer.getSharedLinkItemPresenterData(boxResponse, mock())

        assertEquals(true, result.isSuccess)
        assertEquals(boxResponse.result, result.data)
    }

    @Test
    fun `test shared link transformer failed http not modified`() {
        //configs
        val boxResponse = createFailedBoxItemResponse(mockHttpNotModifiedException)
        val result = shareSDKTransformer.getSharedLinkItemPresenterData(boxResponse, mock())

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(PresenterData.NO_MESSAGE, result.strCode)
    }

    @Test
    fun `test shared link transformer failed http forbidden`() {
        //configs
        val boxResponse = createFailedBoxItemResponse(mockHttpForbiddenException)
        val result = shareSDKTransformer.getSharedLinkItemPresenterData(boxResponse, mock())

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_insufficient_permissions, result.strCode)
    }

    @Test
    fun `test shared link transformer failed generic exception`() {
        //configs
        val boxResponse = createFailedBoxItemResponse(mockGenericException)
        val result = shareSDKTransformer.getSharedLinkItemPresenterData(boxResponse, mock())

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(PresenterData.NO_MESSAGE, result.strCode)
    }

    @Test
    fun `test shared link transformer failed generic exception instance of BoxRequestItem`() {
        //configs
        val boxItem: BoxItem = mock()
        val id = "mockID"
        whenever(boxItem.id).thenReturn(id)
        val boxResponse = createFailedBoxItemResponse(mockGenericException)
        val boxRequestItem: BoxRequestItem<*, *> = mock()
        whenever(boxRequestItem.getId()).thenReturn(id)
        whenever(boxResponse.request).thenReturn(boxRequestItem)
        val result = shareSDKTransformer.getSharedLinkItemPresenterData(boxResponse, boxItem)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_problem_accessing_this_shared_link, result.strCode)
    }

    @Test
    fun `test shared link transformer failed generic exception instance of BoxRequestUpdateShareItem`() {
        //configs
        val boxItem: BoxItem = mock()
        val id = "mockID"
        whenever(boxItem.id).thenReturn(id)
        val boxResponse = createFailedBoxItemResponse(mockGenericException)
        val boxRequestItem: BoxRequestUpdateSharedItem<*, *> = mock()
        whenever(boxRequestItem.getId()).thenReturn(id)
        whenever(boxResponse.request).thenReturn(boxRequestItem)
        val result = shareSDKTransformer.getSharedLinkItemPresenterData(boxResponse, boxItem)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_unable_to_modify_toast, result.strCode)
    }


    @Test
    fun `test delete collaboration transformer success`() {
        //configs
        val boxResponse: BoxResponse<BoxVoid> = mock()
        whenever(boxResponse.isSuccess).thenReturn(true)
        val boxRequest: BoxRequest<*, *> = mock()
        whenever(boxResponse.request).thenReturn(boxRequest)

        val result = shareSDKTransformer.getDeleteCollaborationPresenterData(boxResponse)

        assertEquals(true, result.isSuccess)
        assertEquals(boxRequest, result.data)
    }

    @Test
    fun `test delete collaboration transformer failure`() {
        //configs
        val boxResponse: BoxResponse<BoxVoid> = mock()
        whenever(boxResponse.isSuccess).thenReturn(false)
        whenever(boxResponse.exception).thenReturn(mockGenericException)
        val result = shareSDKTransformer.getDeleteCollaborationPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_network_error, result.strCode)
    }

    @Test
    fun `test update owner transformer success`() {
        //configs
        val boxResponse: BoxResponse<BoxVoid> = mock()
        whenever(boxResponse.isSuccess).thenReturn(true)

        val result = shareSDKTransformer.getUpdateOwnerPresenterData(boxResponse)

        assertEquals(true, result.isSuccess)
        assertEquals(null, result.data)
    }

    @Test
    fun `test update owner transformer failure generic`() {
        //configs
        val boxResponse: BoxResponse<BoxVoid> = mock()
        whenever(boxResponse.isSuccess).thenReturn(false)
        whenever(boxResponse.exception).thenReturn(mockGenericException)
        val result = shareSDKTransformer.getUpdateOwnerPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(PresenterData.NO_MESSAGE, result.strCode)
    }

    @Test
    fun `test update owner transformer failure network error`() {
        //configs
        val boxResponse: BoxResponse<BoxVoid> = mock()
        whenever(boxResponse.isSuccess).thenReturn(false)
        whenever(boxResponse.exception).thenReturn(mockBoxNetworkErrorException)
        val result = shareSDKTransformer.getUpdateOwnerPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_network_error, result.strCode)
    }

    @Test
    fun `test update owner transformer failure new owner not collaborator`() {
        //configs
        val boxResponse: BoxResponse<BoxVoid> = mock()
        whenever(boxResponse.isSuccess).thenReturn(false)
        val exception: BoxException = mock()
        whenever(exception.errorType).thenReturn(BoxException.ErrorType.NEW_OWNER_NOT_COLLABORATOR)
        whenever(boxResponse.exception).thenReturn(exception)
        val result = shareSDKTransformer.getUpdateOwnerPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharedsdk_new_owner_not_collaborator, result.strCode)
    }

    @Test
    fun `test update owner transformer failure new owner default`() {
        //configs
        val boxResponse: BoxResponse<BoxVoid> = mock()
        whenever(boxResponse.isSuccess).thenReturn(false)
        whenever(boxResponse.exception).thenReturn(mockBoxGenericException)
        val result = shareSDKTransformer.getUpdateOwnerPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharedsdk_unable_to_update_owner, result.strCode)
    }

    @Test
    fun `test update collaboration success`() {
        //configs
        val boxResponse: BoxResponse<BoxCollaboration> = mock()
        whenever(boxResponse.isSuccess).thenReturn(true)

        val mockCollaboration: BoxCollaboration = mock()
        whenever(boxResponse.result).thenReturn(mockCollaboration)
        val result = shareSDKTransformer.getUpdateCollaborationPresenterData(boxResponse)

        assertEquals(true, result.isSuccess)
        assertEquals(mockCollaboration, result.data)
        assertEquals(PresenterData.NO_MESSAGE, result.strCode)
    }


    @Test
    fun `test update collaboration transformer failure generic`() {
        //configs
        val boxResponse: BoxResponse<BoxCollaboration> = mock()
        whenever(boxResponse.exception).thenReturn(mockGenericException)
        whenever(boxResponse.isSuccess).thenReturn(false)
        val result = shareSDKTransformer.getUpdateCollaborationPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(PresenterData.NO_MESSAGE, result.strCode)
    }

    @Test
    fun `test update collaboration transformer failure network error`() {
        val boxResponse = createFailedBoxCollaborationResponse("", mockBoxNetworkErrorException)
        whenever(boxResponse.isSuccess).thenReturn(false)
        val result = shareSDKTransformer.getUpdateCollaborationPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_network_error, result.strCode)
    }

    @Test
    fun `test update collaboration transformer failure http forbidden`() {
        val boxResponse = createFailedBoxCollaborationResponse("", mockHttpForbiddenException)
        whenever(boxResponse.isSuccess).thenReturn(false)
        val result = shareSDKTransformer.getUpdateCollaborationPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_insufficient_permissions, result.strCode)
    }

    @Test
    fun `test update collaboration transformer failure default`() {
        //configs


        val boxResponse = createFailedBoxCollaborationResponse("", mockBoxGenericException)
        whenever(boxResponse.isSuccess).thenReturn(false)
        val result = shareSDKTransformer.getUpdateCollaborationPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_cannot_get_collaborators, result.strCode)
    }


    @Test
    fun `test get collaborations success`() {
        //configs
        val boxResponse: BoxResponse<BoxIteratorCollaborations> = mock()
        whenever(boxResponse.isSuccess).thenReturn(true)

        val mockCollaboration: BoxIteratorCollaborations = mock()
        whenever(boxResponse.result).thenReturn(mockCollaboration)
        val result = shareSDKTransformer.getCollaborationsPresenterData(boxResponse)

        assertEquals(true, result.isSuccess)
        assertEquals(mockCollaboration, result.data)
        assertEquals(PresenterData.NO_MESSAGE, result.strCode)
    }


    @Test
    fun `test get collaboration transformer failure generic`() {
        //configs
        val boxResponse: BoxResponse<BoxIteratorCollaborations> = createFailedBoxIterCollaborationResponse(mockBoxGenericException)
        val result = shareSDKTransformer.getCollaborationsPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_cannot_get_collaborators, result.strCode)
    }

    @Test
    fun `test get collaboration transformer failure network error`() {
        //configs
        val boxResponse = createFailedBoxIterCollaborationResponse(mockBoxNetworkErrorException)
        whenever(boxResponse.isSuccess).thenReturn(false)
        val result = shareSDKTransformer.getCollaborationsPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_network_error, result.strCode)
    }

    @Test
    fun `test get collaboration transformer failure http forbidden`() {
        val boxResponse = createFailedBoxIterCollaborationResponse(mockHttpForbiddenException)
        whenever(boxResponse.isSuccess).thenReturn(false)
        val result = shareSDKTransformer.getCollaborationsPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_insufficient_permissions, result.strCode)
    }

    @Test
    fun `test get collaboration transformer failure default`() {
        //configs
        val boxResponse = createFailedBoxIterCollaborationResponse(mockBoxGenericException)
        whenever(boxResponse.isSuccess).thenReturn(false)
        val result = shareSDKTransformer.getCollaborationsPresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_cannot_get_collaborators, result.strCode)
    }

    @Test
    fun `test get initials collab transformer success` () {
        val boxResponse: BoxResponse<BoxIteratorCollaborations> = mock()
        whenever(boxResponse.isSuccess).thenReturn(true)
        val boxIterCollab: BoxIteratorCollaborations = mock()
        whenever(boxResponse.result).thenReturn(boxIterCollab)

        val result = shareSDKTransformer.getIntialsViewCollabsPresenterData(boxResponse, null)

        assertEquals(true, result.isSuccess)
        assertEquals(boxIterCollab, result.data)
    }

    @Test
    fun `test get initials collab transformer failure http not found` () {
        val boxException: BoxException = mock()
        whenever(boxException.responseCode).thenReturn(HTTP_NOT_FOUND)
        val boxResponse = createFailedBoxIterCollaborationResponse(boxException)
        val result = shareSDKTransformer.getIntialsViewCollabsPresenterData(boxResponse, null)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
        assertEquals(R.string.box_sharesdk_item_unavailable, result.strCode)
    }


    @Test
    fun `test get initials collab transformer failure generic cached` () {
        val mockData: BoxIteratorCollaborations = mock()
        val boxResponse = createFailedBoxIterCollaborationResponse(mockGenericException)
        val result = shareSDKTransformer.getIntialsViewCollabsPresenterData(boxResponse, mockData)

        assertEquals(false, result.isSuccess)
        assertEquals(mockData, result.data)
        assertEquals(PresenterData.NO_MESSAGE, result.strCode)
    }
    @Test
    fun `test get supported features transformer success` () {
        val boxResponse: BoxResponse<BoxFeatures> = mock()
        val mockBoxFeatures: BoxFeatures = mock()
        whenever(boxResponse.isSuccess).thenReturn(true)
        whenever(boxResponse.result).thenReturn(mockBoxFeatures)
        val result = shareSDKTransformer.getSupportedFeaturePresenterData(boxResponse)

        assertEquals(true, result.isSuccess)
        assertEquals(mockBoxFeatures, result.data)
    }

    @Test
    fun `test get supported features transformer failure` () {
        val boxResponse: BoxResponse<BoxFeatures> = mock()
        whenever(boxResponse.exception).thenReturn(mock())
        whenever(boxResponse.isSuccess).thenReturn(false)
        val result = shareSDKTransformer.getSupportedFeaturePresenterData(boxResponse)

        assertEquals(false, result.isSuccess)
        assertEquals(null, result.data)
    }
}
