import com.stephen.lab.Application;
import com.stephen.lab.model.User;
import com.stephen.lab.service.UserService;
import com.stephen.lab.util.LogRecod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class IndexTestController {
    @Autowired
    private UserService userService;
    @Test
    public void testUser(){
        User user=userService.getUser(1);
        LogRecod.info(user);
    }
}
