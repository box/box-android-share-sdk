package com.box.androidsdk.share.utils;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.content.models.BoxError
import com.box.androidsdk.content.requests.BoxRequestsShare
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.R
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createBoxResponseBatch
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createFailureResponse
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createSuccessResponse
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test;
import org.junit.rules.TestRule
import javax.net.ssl.HttpsURLConnection

class ShareSDKTransformerTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val mockFailedToAddException: BoxException = mock()
    private val mockHttpForbiddenException: BoxException = mock()
    private val mockBoxNetworkErrorException: BoxException = mock()
    private val mockAlreadyCollabException: BoxException = mock()
    private val mockBadRequestException: BoxException = mock()

    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockInviteCollabsResponse: BoxResponse<BoxResponseBatch> = mock()

    private val mockGetInviteesResult: BoxIteratorInvitees = mock()
    private val mockFetchRoleItemResult: BoxCollaborationItem = mock()
    private lateinit var mockInviteCollabsResult: BoxResponseBatch
    
    private val inviteCollabsTransformer = ShareSDKTransformer()


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
    }

    @Test
    fun `test fetch role success`() {
        //configs
        whenever(mockFetchRolesResponse.isSuccess).thenReturn(true)
        whenever(mockFetchRolesResponse.result).thenReturn(mockFetchRoleItemResult)

        //make a network call to fetch roles
        val result = inviteCollabsTransformer.getFetchRolesItemPresenterData(mockFetchRolesResponse)

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
        val result = inviteCollabsTransformer.getFetchRolesItemPresenterData(mockFetchRolesResponse)

        assertEquals(false, result?.isSuccess)
        assertEquals(R.string.box_sharesdk_network_error, result?.strCode)
    }

    @Test
    fun `test get invitees success`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(true)
        whenever(mockGetInviteeResponse.result).thenReturn(mockGetInviteesResult)

        //make a network call to fetch roles
        val result = inviteCollabsTransformer.getInviteesPresenterData(mockGetInviteeResponse)

        assertEquals(true, result?.isSuccess)
        assertEquals(mockGetInviteesResult, result?.data)
    }

    @Test
    fun `test get invitees failure http forbidden`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockHttpForbiddenException)

        //make a network call to fetch roles
        val result = inviteCollabsTransformer.getInviteesPresenterData(mockGetInviteeResponse)

        assertEquals(false, result?.isSuccess)
        assertEquals(R.string.box_sharesdk_insufficient_permissions, result?.strCode)
    }

    @Test
    fun `test get invitees failure box network error`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockBoxNetworkErrorException)

        //make a network call to fetch roles
        val result = inviteCollabsTransformer.getInviteesPresenterData(mockGetInviteeResponse)

        assertEquals(false, result?.isSuccess)
        assertEquals(R.string.box_sharesdk_network_error, result?.strCode)
    }


    @Test
    fun `test get invite collabs presenter data for successful request where response size is 1 and accessible by is not null`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createSuccessResponse(dummyName)

        val boxResponses = createBoxResponseBatch(boxResponse)

        //process request
        val result = inviteCollabsTransformer.getPresenterDataForSuccessfulRequest(boxResponses)

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
        val boxResponse = createSuccessResponse(dummyName)

        val boxResponses = createBoxResponseBatch(boxResponse)

        //process request
        val result = inviteCollabsTransformer.getPresenterDataForSuccessfulRequest(boxResponses)


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
        val boxResponse: BoxResponse<BoxCollaboration> = createSuccessResponse(dummyName)
        val boxResponse2: BoxResponse<BoxCollaboration> = createSuccessResponse(dummyName+"2")
        val boxResponses = createBoxResponseBatch(boxResponse, boxResponse2)

        //process request
        val result = inviteCollabsTransformer.getPresenterDataForSuccessfulRequest(boxResponses)

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
        val result = inviteCollabsTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

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
        val result = inviteCollabsTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.plurals.box_sharesdk_already_been_invited, result.strCode)
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
        val result = inviteCollabsTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.plurals.box_sharesdk_already_been_invited, result.strCode)
        assertEquals(dummyName, result.data)
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
        val result = inviteCollabsTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

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
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createSuccessResponse("user2")
        val boxResponse3 = createSuccessResponse("user3")
        mockInviteCollabsResult = createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = inviteCollabsTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

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
        val boxResponse = createSuccessResponse(dummyName)
        mockInviteCollabsResult = createBoxResponseBatch(boxResponse)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = inviteCollabsTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

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
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureResponse(dummyName, mockAlreadyCollabException)
        mockInviteCollabsResult = createBoxResponseBatch(boxResponse, boxResponse2)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = inviteCollabsTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

        assertEquals(R.plurals.box_sharesdk_already_been_invited, result.strCode)
        assertEquals(dummyName, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(1, result.alreadyAdddedCount)
    }

    @Test
    fun `test add collab succeed 1 already add 2`() {
        //configs
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureResponse("user2", mockAlreadyCollabException)
        val boxResponse3 = createFailureResponse("user3", mockAlreadyCollabException)
        mockInviteCollabsResult= createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = inviteCollabsTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

        assertEquals(R.plurals.box_sharesdk_already_been_invited, result.strCode)
        assertEquals("user3", result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(2, result.alreadyAdddedCount)
    }

    @Test
    fun `test add collab succeed 1 failed to add 1 already added 1`() {
        //configs
        val failedName = "user2"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureResponse(failedName, mockFailedToAddException)
        val boxResponse3 = createFailureResponse("user3", mockAlreadyCollabException)
        mockInviteCollabsResult = createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = inviteCollabsTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)

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
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureResponse(failedName1, mockFailedToAddException)
        val boxResponse3 = createFailureResponse(failedName2, mockFailedToAddException)
        mockInviteCollabsResult = createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockInviteCollabsResponse.result).thenReturn(mockInviteCollabsResult)

        //process request
        val result = inviteCollabsTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)


        assertEquals(R.string.box_sharesdk_following_collaborators_error, result.strCode)
        assertEquals("$failedName1 $failedName2", result.data)
        assertEquals(false, result.isSuccess)
        assertEquals(true, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

}
