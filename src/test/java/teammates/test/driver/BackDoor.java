package teammates.test.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.CourseAttributes;
import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.FeedbackQuestionAttributes;
import teammates.common.datatransfer.FeedbackResponseAttributes;
import teammates.common.datatransfer.FeedbackSessionAttributes;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.datatransfer.StudentProfileAttributes;
import teammates.common.exception.TeammatesException;
import teammates.common.util.Const;
import teammates.common.util.Utils;
import teammates.logic.backdoor.BackDoorServlet;

import com.google.gson.reflect.TypeToken;

/**
 * Used to access the datastore without going through the UI. The main use of
 * this class is for the test suite to prepare test data. <br>
 * It works only if the test.backdoor.key in test.properties matches the
 * app.backdoor.key in build.properties of the deployed app. Using this
 * mechanism we can limit back door access to only the person who deployed the
 * application.
 * 
 */
public final class BackDoor {
    
    private BackDoor() {
        //utility class
    }

    /**
     * Persists given data. If given entities already exist in the data store,
     * they will be overwritten.
     */
    public static String restoreDataBundle(DataBundle dataBundle) {
        String dataBundleJson = Utils.getTeammatesGson().toJson(dataBundle);
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_PERSIST_DATABUNDLE);
        params.put(BackDoorServlet.PARAMETER_DATABUNDLE_JSON, dataBundleJson);
        return makePostRequest(params);
    }
    
    /**
     * Removes given data. If given entities have already been deleted,
     * it fails silently
     */
    public static String removeDataBundleFromDb(DataBundle dataBundle) {
        String dataBundleJson = Utils.getTeammatesGson().toJson(dataBundle);
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_REMOVE_DATABUNDLE);
        params.put(BackDoorServlet.PARAMETER_DATABUNDLE_JSON, dataBundleJson);
        return makePostRequest(params);
    }
    
    /**
     * Removes and restores given data.
     */
    public static String removeAndRestoreDataBundleFromDb(DataBundle dataBundle) {
        String dataBundleJson = Utils.getTeammatesGson().toJson(dataBundle);
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_REMOVE_AND_RESTORE_DATABUNDLE);
        params.put(BackDoorServlet.PARAMETER_DATABUNDLE_JSON, dataBundleJson);
        return makePostRequest(params);
    }

    /**
     * This create documents for entities through back door.
     */
    public static String putDocuments(DataBundle dataBundle) {
        String dataBundleJson = Utils.getTeammatesGson().toJson(dataBundle);
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_PUT_DOCUMENTS);
        params.put(BackDoorServlet.PARAMETER_DATABUNDLE_JSON, dataBundleJson);
        return makePostRequest(params);
    }

    public static String createAccount(AccountAttributes account) {
        DataBundle dataBundle = new DataBundle();
        dataBundle.accounts.put(account.googleId, account);
        return restoreDataBundle(dataBundle);
    }
    
    public static AccountAttributes getAccount(String googleId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_ACCOUNT_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_GOOGLE_ID, googleId);
        String accountJsonString = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(accountJsonString, AccountAttributes.class);
    }
    
    public static StudentProfileAttributes getStudentProfile(String googleId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_STUDENTPROFILE_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_GOOGLE_ID, googleId);
        String studentProfileJsonString = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(studentProfileJsonString, StudentProfileAttributes.class);
    }
    
    public static boolean isPicturePresentInGcs(String pictureKey) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_IS_PICTURE_PRESENT_IN_GCS);
        params.put(BackDoorServlet.PARAMETER_PICTURE_KEY, pictureKey);
        return Boolean.parseBoolean(makePostRequest(params));
    }

    public static String uploadAndUpdateStudentProfilePicture(String googleId, String pictureKey) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_EDIT_STUDENT_PROFILE_PICTURE);
        params.put(BackDoorServlet.PARAMETER_GOOGLE_ID, googleId);
        params.put(BackDoorServlet.PARAMETER_PICTURE_DATA, pictureKey);
        return makePostRequest(params);
    }

    public static String deleteAccount(String googleId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_DELETE_ACCOUNT);
        params.put(BackDoorServlet.PARAMETER_GOOGLE_ID, googleId);
        return makePostRequest(params);
    }
    
    public static String createInstructor(InstructorAttributes instructor) {
        DataBundle dataBundle = new DataBundle();
        dataBundle.instructors.put(instructor.googleId, instructor);
        return restoreDataBundle(dataBundle);
    }

    public static InstructorAttributes getInstructorByGoogleId(String instructorId, String courseId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_INSTRUCTOR_AS_JSON_BY_ID);
        params.put(BackDoorServlet.PARAMETER_INSTRUCTOR_ID, instructorId);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        String instructorJsonString = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(instructorJsonString, InstructorAttributes.class);
    }
    
    public static InstructorAttributes getInstructorByEmail(String instructorEmail, String courseId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_INSTRUCTOR_AS_JSON_BY_EMAIL);
        params.put(BackDoorServlet.PARAMETER_INSTRUCTOR_EMAIL, instructorEmail);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        String instructorJsonString = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(instructorJsonString, InstructorAttributes.class);
    }
    
    public static String getEncryptedKeyForInstructor(String courseId, String instructorEmail) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_ENCRYPTED_KEY_FOR_INSTRUCTOR);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        params.put(BackDoorServlet.PARAMETER_INSTRUCTOR_EMAIL, instructorEmail);
        return makePostRequest(params);

    }

    public static String deleteInstructor(String courseId, String instructorEmail) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_DELETE_INSTRUCTOR);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        params.put(BackDoorServlet.PARAMETER_INSTRUCTOR_EMAIL, instructorEmail);
        return makePostRequest(params);
    }

    public static String createCourse(CourseAttributes course) {
        DataBundle dataBundle = new DataBundle();
        dataBundle.courses.put("dummy-key", course);
        return restoreDataBundle(dataBundle);
    }

    public static CourseAttributes getCourse(String courseId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_COURSE_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        String courseJsonString = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(courseJsonString, CourseAttributes.class);
    }
    
    public static String deleteCourse(String courseId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_DELETE_COURSE);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        return makePostRequest(params);
    }

    public static String createStudent(StudentAttributes student) {
        DataBundle dataBundle = new DataBundle();
        dataBundle.students.put("dummy-key", student);
        return restoreDataBundle(dataBundle);
    }

    public static StudentAttributes getStudent(String courseId, String studentEmail) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_STUDENT_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        params.put(BackDoorServlet.PARAMETER_STUDENT_EMAIL, studentEmail);
        String studentJson = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(studentJson, StudentAttributes.class);
    }

    public static String getEncryptedKeyForStudent(String courseId, String studentEmail) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_ENCRYPTED_KEY_FOR_STUDENT);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        params.put(BackDoorServlet.PARAMETER_STUDENT_EMAIL, studentEmail);
        return makePostRequest(params);
    }

    public static String editStudent(String originalEmail, StudentAttributes student) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_EDIT_STUDENT);
        params.put(BackDoorServlet.PARAMETER_STUDENT_EMAIL, originalEmail);
        params.put(BackDoorServlet.PARAMETER_JSON_STRING, Utils
                .getTeammatesGson().toJson(student));
        return makePostRequest(params);
    }

    public static String deleteStudent(String courseId, String studentEmail) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_DELETE_STUDENT);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        params.put(BackDoorServlet.PARAMETER_STUDENT_EMAIL, studentEmail);
        return makePostRequest(params);
    }

    public static FeedbackSessionAttributes getFeedbackSession(String courseId,
            String feedbackSessionName) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_FEEDBACK_SESSION_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_FEEDBACK_SESSION_NAME, feedbackSessionName);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        String feedbackSessionJson = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(feedbackSessionJson, FeedbackSessionAttributes.class);
    }
    
    public static String editFeedbackSession(FeedbackSessionAttributes updatedFeedbackSession) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_EDIT_FEEDBACK_SESSION);
        params.put(BackDoorServlet.PARAMETER_JSON_STRING, Utils
                .getTeammatesGson().toJson(updatedFeedbackSession));
        return makePostRequest(params);
    }
    
    public static String deleteFeedbackSession(String feedbackSessionName,
            String courseId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_DELETE_FEEDBACK_SESSION);
        params.put(BackDoorServlet.PARAMETER_FEEDBACK_SESSION_NAME, feedbackSessionName);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        return makePostRequest(params);
    }

    public static FeedbackQuestionAttributes getFeedbackQuestion(String courseId,
            String feedbackSessionName, int qnNumber) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_FEEDBACK_QUESTION_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        params.put(BackDoorServlet.PARAMETER_FEEDBACK_SESSION_NAME, feedbackSessionName);
        params.put(BackDoorServlet.PARAMETER_FEEDBACK_QUESTION_NUMBER, qnNumber);
        String feedbackQuestionJson = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(feedbackQuestionJson, FeedbackQuestionAttributes.class);
    }
    
    public static FeedbackQuestionAttributes getFeedbackQuestion(String questionId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_FEEDBACK_QUESTION_FOR_ID_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_FEEDBACK_QUESTION_ID, questionId);
        String feedbackQuestionJson = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(feedbackQuestionJson, FeedbackQuestionAttributes.class);
    }
    
    public static String editFeedbackQuestion(FeedbackQuestionAttributes updatedFeedbackQuestion) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_EDIT_FEEDBACK_QUESTION);
        params.put(BackDoorServlet.PARAMETER_JSON_STRING, Utils
                .getTeammatesGson().toJson(updatedFeedbackQuestion));
        return makePostRequest(params);
    }

    public static String deleteFeedbackQuestion(String questionId) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_DELETE_FEEDBACK_QUESTION);
        params.put(BackDoorServlet.PARAMETER_FEEDBACK_QUESTION_ID, questionId);
        return makePostRequest(params);
    }

    public static String createFeedbackResponse(FeedbackResponseAttributes feedbackResponse) {
        DataBundle dataBundle = new DataBundle();
        dataBundle.feedbackResponses.put("dummy-key", feedbackResponse);
        return restoreDataBundle(dataBundle);
    }
    
    public static FeedbackResponseAttributes getFeedbackResponse(String feedbackQuestionId,
            String giverEmail, String recipient) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_GET_FEEDBACK_RESPONSE_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_FEEDBACK_QUESTION_ID, feedbackQuestionId);
        params.put(BackDoorServlet.PARAMETER_GIVER_EMAIL, giverEmail);
        params.put(BackDoorServlet.PARAMETER_RECIPIENT, recipient);
        String feedbackResponseJson = makePostRequest(params);
        return Utils.getTeammatesGson().fromJson(feedbackResponseJson, FeedbackResponseAttributes.class);
    }
    
    public static List<FeedbackResponseAttributes> getFeedbackResponsesForReceiverForCourse(
            String courseId, String recipientEmail) {
        HashMap<String, Object> params = createParamMap(
                BackDoorServlet.OPERATION_GET_FEEDBACK_RESPONSES_FOR_RECEIVER_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        params.put(BackDoorServlet.PARAMETER_RECIPIENT, recipientEmail);
        String feedbackResponsesJson = makePostRequest(params);
        return Utils.getTeammatesGson()
                .fromJson(feedbackResponsesJson, new TypeToken<List<FeedbackResponseAttributes>>(){}
                .getType());
    }
    
    public static List<FeedbackResponseAttributes> getFeedbackResponsesFromGiverForCourse(
            String courseId, String giverEmail) {
        HashMap<String, Object> params = createParamMap(
                BackDoorServlet.OPERATION_GET_FEEDBACK_RESPONSES_FOR_GIVER_AS_JSON);
        params.put(BackDoorServlet.PARAMETER_COURSE_ID, courseId);
        params.put(BackDoorServlet.PARAMETER_GIVER_EMAIL, giverEmail);
        String feedbackResponsesJson = makePostRequest(params);
        return Utils.getTeammatesGson()
                .fromJson(feedbackResponsesJson, new TypeToken<List<FeedbackResponseAttributes>>(){}
                .getType());
    }

    public static String deleteFeedbackResponse(String feedbackQuestionId,
                                              String giverEmail,
                                              String recipient) {
        HashMap<String, Object> params = createParamMap(BackDoorServlet.OPERATION_DELETE_FEEDBACK_RESPONSE);
        params.put(BackDoorServlet.PARAMETER_FEEDBACK_QUESTION_ID, feedbackQuestionId);
        params.put(BackDoorServlet.PARAMETER_GIVER_EMAIL, giverEmail);
        params.put(BackDoorServlet.PARAMETER_RECIPIENT, recipient);
        return makePostRequest(params);
    }

    private static HashMap<String, Object> createParamMap(String operation) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(BackDoorServlet.PARAMETER_BACKDOOR_OPERATION, operation);

        // For Authentication
        map.put(BackDoorServlet.PARAMETER_BACKDOOR_KEY,
                TestProperties.BACKDOOR_KEY);

        return map;
    }

    private static String makePostRequest(HashMap<String, Object> map) {
        try {
            String paramString = encodeParameters(map);
            String urlString = TestProperties.TEAMMATES_URL + Const.ActionURIs.BACKDOOR;
            URLConnection conn = getConnectionToUrl(urlString);
            sendRequest(paramString, conn);
            return readResponse(conn);
        } catch (Exception e) {
            return TeammatesException.toStringWithStackTrace(e);
        }
    }

    private static String readResponse(URLConnection conn) throws IOException {
        conn.setReadTimeout(10000);
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                conn.getInputStream(), "UTF-8"));
        
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        return sb.toString();
    }

    private static void sendRequest(String paramString, URLConnection conn)
            throws IOException {
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
        wr.write(paramString);
        wr.flush();
        wr.close();
    }

    private static URLConnection getConnectionToUrl(String urlString)
            throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        return conn;
    }

    private static String encodeParameters(HashMap<String, Object> map)
            throws UnsupportedEncodingException {
        StringBuilder dataStringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            dataStringBuilder.append(URLEncoder.encode(e.getKey(), "UTF-8")
                    + "=" + URLEncoder.encode(e.getValue().toString(), "UTF-8")
                    + "&");
        }
        return dataStringBuilder.toString();
    }
}
