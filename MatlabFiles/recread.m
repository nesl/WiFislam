javaaddpath('+json/java/json.jar');
% Opening file, parsing JSON

disp('Attempting to open file.')
file_id = fopen('records.txt');

line = fgetl(file_id);
line_num = 1;
xloc = 0;
yloc = 0;
records = cell(1);

tic
disp('Parsing JSONs.')
while ischar(line)
    records{line_num} = json.load(line);
    line = fgetl(file_id);
    line_num = line_num + 1;
end
    
fclose(file_id);

clear file_id line line_num scan_record
toc

disp('Collecting analysis information.')
% Collecting information for processing

x_loc = 0;
y_loc = 0;

seg_1 = 10; % metres
seg_2 = 25;
seg_3 = 25;
seg_4 = 25;
seg_5 = 15;

time_1 = 0;
time_2 = 0;
time_3 = 0;
time_4 = 0;
time_5 = 0;

vel_1 = 0;
vel_2 = 0;
vel_3 = 0;
vel_4 = 0;
vel_5 = 0;

rcds_size = size(records,2);
last_m = 0;
last_time = records{1}.timestamp;
first_time = last_time;

for r=1:rcds_size
    record = records{r};
    m_loc = record.mark;
    if(last_m < m_loc)
        time = record.timestamp - last_time;
        fprintf('Marked at: %f\tDiff: %f\n',record.timestamp,time);
        last_m = m_loc;
        last_time = record.timestamp;
        section = int2str(m_loc);
        %fprintf('time_%d = time;',m_loc+1);
        eval(['time_' section ' = last_time;']);
        eval(['vel_' section ' = seg_' section '/(time/1000);']);
    end
end

% Write to cell arrays
disp('Writing to array.')

prc = cell(4);
for r=1:rcds_size
    record = records{r};
    prc{r,1} = (record.timestamp - first_time)/1000;
    prc{r,4} = record.mark;
    
    unique_net = record.networks;
    unique_size = size(unique_net,2);
    filter = 1;
    matches = 0;
    while filter <= unique_size
        mac = unique_net(filter).bssid;
        check = filter + 1;
        %matches = 0;
        while check <= unique_size
            if(mac == unique_net(check).bssid)
                %matches = matches + 1;
                %fprintf('found match: %s - %s\n',mac,unique_net(check).bssid);
                unique_net(check) = [];
                check = check - 1;
                unique_size = unique_size - 1;
            end
            check = check + 1;
        end
        filter = filter + 1;
    end
    %fprintf('found %d matches for record %d.\n',matches,r);
    prc{r,5} = unique_net;
    m_loc = record.mark;
    % Determine x and y:
    if(m_loc == 0)
        x_loc = 0;
        y_loc = ((record.timestamp - first_time)/1000)*vel_1;
    elseif(m_loc == 1)
        x_loc = 0 - ((record.timestamp - time_1)/1000)*vel_2;
        y_loc = seg_1;
    elseif(m_loc == 2)
        x_loc = 0 - seg_2;
        y_loc = seg_1 - ((record.timestamp - time_2)/1000)*vel_3;
    elseif(m_loc == 3)
        x_loc = 0 - seg_2;
        y_loc = seg_1 - seg_3 - ((record.timestamp - time_3)/1000)*vel_4;
    elseif(m_loc == 4)
        x_loc = -seg_2 + ((record.timestamp - time_4)/1000)*vel_5;
        y_loc = seg_1 - seg_3 - seg_4;
    elseif(m_loc == 5)
        x_loc = -seg_2 + seg_5;
        y_loc = seg_1 - seg_3 - seg_4;
    end
    prc{r,2} = round(x_loc*100)/100;
    prc{r,3} = round(y_loc*100)/100;
end

disp('Cleaning.')
clearvars -except prc %time_1 time_2 time_3 time_4 time_5 vel_1 vel_2 vel_3 vel_4 vel_5