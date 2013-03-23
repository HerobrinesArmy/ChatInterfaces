require 'net/http'
require 'json'

module ChatInterfaces
    class Network
        HOST = 'http://herobrinesarmy.com/'
        AUTH = 'auth.php'
        AUTH_CHECK = 'amiauth'
        LOGOUT = 'logout'
        POST = 'post_chat.php'
        MESSAGES = 'update_chat2.php'
        class << self 
            def fetch(url, params: {}, headers: {}, get: true) # I don't think we need to follow redirections.
                uri = URI(url)                                                        # That functionality can be added later if needed.
                Net::HTTP.start(uri.host, uri.port) do |connection|
                    if get
                        uri.query = URI.encode_www_form(params)
                        req = Net::HTTP::Get.new(uri)
                    else
                        req = Net::HTTP::Post.new(uri)
                        req.set_form_data(params)
                    end
                    headers.each { |k, v| req[k] = v }
                    connection.request(req)
                end
            end
            
            def check_auth(cookie)
                fetch(HOST + AUTH_CHECK, headers: { 'Cookie' => cookie }).body == 'Yeah.'
            end
            
            def login(user, pass)
                cookies = fetch(HOST + AUTH,  params: { 'user' => user, 'pass' => pass }, get: false).get_fields('set-cookie')
                cookie_array = []
                cookies.each { |cookie| cookie_array << cookie.split('; ').first }
                cookie_array * '; '
            end
            
            def logout(cookie)
                fetch(HOST + LOGOUT, headers: { 'Cookie' => cookie })
            end
            
            def send_message(message, room, cookie)
                fetch(HOST + POST, params: { 'o' => 1, 'c' => room, 'm' => message }, headers: { 'Cookie' => cookie })
            end
            
            def get_messages(room, cookie, lmid = 0)
                json = fetch(HOST + MESSAGES, params: { 'p' => 0,'c' => room, 'l' => lmid }, headers: { 'Cookie' => cookie }).body
                return nil if json.empty?
                json = json[1..-2] if /\(.*\)/.match(json)
                data = JSON.parse(json)
            end
        end
    end
end