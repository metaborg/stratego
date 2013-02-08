package example;

class User {
    
    private String name;
    
    public String get_name {
        return name;
    }
    
    public void set_name (String name) {
        this.name = name;    
    }
    
    private String password;
    
    public String get_password {
        return password;
    }
    
    public void set_password (String password) {
        this.password = password;    
    }
    
    private URL homepage;
    
    public URL get_homepage {
        return homepage;
    }
    
    public void set_homepage (URL homepage) {
        this.homepage = homepage;    
    }
    
}
class BlogPosting {
    
    private User poster;
    
    public User get_poster {
        return poster;
    }
    
    public void set_poster (User poster) {
        this.poster = poster;    
    }
    
    private String body;
    
    public String get_body {
        return body;
    }
    
    public void set_body (String body) {
        this.body = body;    
    }
    
}
class URL {
    
    private String location;
    
    public String get_location {
        return location;
    }
    
    public void set_location (String location) {
        this.location = location;    
    }
    
}

