module MusicHelper
  def select_tag_remote objects
    result = '<select id="select" size="10" name="'+objects[0].name+'" multiple="multiple">
      <option value="-1">Alle</option>'
    objects.each do |object|
      result += '<option value="'+object.id+'">'+object.name+'</option>'
    end
    result +=  '</select>'
    return result
  end
end
