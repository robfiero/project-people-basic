package com.people.cli;

import cli.CliEngine;
import cli.CommandRegistry;
import cli.commands.ExitCommand;
import cli.commands.HelpCommand;
import com.people.api.PeopleApi;
import com.people.cli.commands.AddressCommand;
import com.people.cli.commands.CompanyCommand;
import com.people.cli.commands.EmploymentCommand;
import com.people.cli.commands.PersonCommand;
import com.people.cli.commands.RelationshipCommand;
import com.people.service.PeopleService;

public final class PeopleSeededCliMain {
    public static void main(String[] args) throws Exception {
        PeopleApi api = PeopleService.createInMemory();
        SeedData.populate(api);

        CommandRegistry registry = new CommandRegistry()
                .register(new HelpCommand())
                .register(new ExitCommand())
                .register(new PersonCommand(api))
                .register(new AddressCommand(api))
                .register(new EmploymentCommand(api))
                .register(new RelationshipCommand(api))
                .register(new CompanyCommand(api));

        CliEngine engine = new CliEngine(registry, "people");
        engine.run();
    }
}
