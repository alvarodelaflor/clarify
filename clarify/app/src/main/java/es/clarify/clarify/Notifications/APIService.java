package es.clarify.clarify.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAE6Zj-Ys:APA91bFy0CgKyY-o5e8wKgKT1KsBWNHOrC7QZNmu4BOR7aKzcwqKIGUm7xNz5w0sHkIA3EiCR5xJF3SvTGE9nwyXNvb0Cn7FzNATlGcaW9a5du6UK9RkbNESKNnQXehkan9HIL_NI8gc"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
