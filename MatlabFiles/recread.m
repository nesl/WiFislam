%RECREAD Summary of this script goes here
%   MAKE SURE filename is set.
%   Reads file line by line, loads JSON object
%   Outputs JSON contents to 'result' array:
%   [timestamp ground_truth_x ground_truth_y network_struct

file_id = fopen(filename);

line = fgetl(file_id);
line_num = 1;
xloc = 0;
yloc = 0;
result = cell(1,4);
while ischar(line)
    scan_record = json.load(line);
    result{line_num,1} = scan_record.timestamp;

    % Some processing has to be done here to figure out actual
    % x-location and y-location. 'Mark' can be accessed by using
    % scan_record.mark

    result{line_num,2} = xloc;
    result{line_num,3} = yloc;
    result{line_num,4} = scan_record.networks;
    line = fgetl(file_id);
    line_num = line_num + 1;
end
    
fclose(file_id);

clear file_id line line_num scan_record xloc yloc