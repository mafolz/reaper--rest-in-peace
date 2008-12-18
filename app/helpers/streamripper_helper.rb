module StreamripperHelper
  def detect online
    @online = true
    if os=="linux"
      if `pidof streamripper` == ''
        @online = false
      end 
    else
      @online = false
    end
  end
end
